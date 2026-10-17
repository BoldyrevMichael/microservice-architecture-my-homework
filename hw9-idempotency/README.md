# Идемпотентное создание заказа

Сервисы из hw8 без изменений, кроме order-service: метод `POST /orders` стал идемпотентным.
Повтор запроса с тем же ключом не создаёт второй заказ, не списывает деньги второй раз и не
запускает сагу повторно, а возвращает уже созданный заказ.

## Паттерн: ключ идемпотентности

Клиент присылает в заголовке `Idempotency-Key` уникальное значение на каждое намерение
создать заказ (обычно UUID) и повторяет запрос с тем же ключом, если не дождался ответа.
Сервис хранит таблицу `idempotency_keys (user_id, key, request_hash, order_id)`:

| Ситуация | Что делает сервис | Ответ |
|---|---|---|
| ключа нет в таблице | создаёт заказ, запоминает ключ, отпечаток тела и id заказа в одной транзакции, публикует `order.created` | 201 заказ |
| ключ есть, отпечаток тела совпадает | ничего не создаёт, возвращает сохранённый заказ в его текущем статусе | 201 тот же заказ |
| ключ есть, тело другое | отказывает: клиент пытается переиспользовать ключ для другого заказа | 422 |
| заголовка нет | отказывает | 400 |

Ключ привязан к пользователю (`X-User-Id` от шлюза), поэтому ключи разных пользователей не
пересекаются. Отпечаток тела — SHA-256 от полей запроса. Запись ключа и заказа идёт в одной
транзакции базы заказов, так что два одновременных запроса с одним ключом упрутся в первичный
ключ `(user_id, key)`, и второй завершится ошибкой вместо дубля.

Что идемпотентно уже с hw8 и почему этого не хватало. В очередях ключ сообщений во всех темах
саги — `orderId`, а переходы заказа однонаправленные: событие для уже завершённого заказа
игнорируется, поэтому повторная доставка события не ломает состояние. Но HTTP-вход саги
защищён не был: два одинаковых `POST /orders` давали два заказа и два списания. Ключ
идемпотентности закрывает именно эту дыру.

## API

Как в hw8, изменился только `POST /orders`:

| Метод | Путь | Заголовки | Ответ |
|---|---|---|---|
| `POST /orders` | `{price, productId, quantity, slot}` | `Authorization: Bearer …`, `Idempotency-Key: <до 128 символов>` | 201 заказ; повтор — 201 тот же заказ; другое тело — 422; без ключа — 400 |

## Состав

- `services/order-service/` — пакет `idempotency` (сущность ключа, репозиторий, отпечаток
  запроса, исключение), миграция `V3__idempotency_keys.sql`, проверка ключа в
  `OrderService.create`. Остальные сервисы как в hw8, образы с тегом `hw9`.
- `k8s/`, `kafka/`, `db/`, `docs/` — как в hw8.
- `postman/order-idempotency.postman_collection.json` — сценарий; `postman/newman-output.txt`
  — вывод прогона.

## Установка

Namespace приложения `msa`, операторы в `cnpg-system` и `kafka`; команды те же, что в hw8:

```bash
helm repo add cnpg https://cloudnative-pg.github.io/charts
helm repo add strimzi https://strimzi.io/charts/
helm repo update
helm install cnpg cnpg/cloudnative-pg --namespace cnpg-system --create-namespace -f db/operator-values.yaml
kubectl -n cnpg-system rollout status deployment/cnpg-cloudnative-pg
helm install strimzi strimzi/strimzi-kafka-operator --namespace kafka --create-namespace \
  --set watchNamespaces="{msa}" \
  --set resources.requests.cpu=100m --set resources.requests.memory=256Mi \
  --set resources.limits.cpu=500m --set resources.limits.memory=384Mi
kubectl -n kafka rollout status deployment/strimzi-cluster-operator

kubectl apply -f k8s/00-namespace.yaml -f k8s/01-secrets.yaml
helm install msa-db cnpg/cluster --namespace msa -f db/values.yaml
kubectl apply -f kafka/kafka.yaml
kubectl -n msa wait cluster/msa-db --for=condition=Ready --timeout=300s
kubectl -n msa wait kafka/msa-kafka --for=condition=Ready --timeout=600s

kubectl apply -f k8s/
kubectl -n msa wait job --all --for=condition=complete --timeout=300s
kubectl -n msa rollout status deployment --timeout=300s
```

Образы собираются и публикуются из каждого `services/<имя>`: `./gradlew build`,
`docker build --platform linux/amd64 -t boldyrevma/<имя>:hw9 .`, `docker push boldyrevma/<имя>:hw9`.

## Проверка

```bash
newman run postman/order-idempotency.postman_collection.json --verbose
```

Сценарий: регистрация, вход, пополнение на 100; заказ без ключа — 400; заказ с ключом — 201;
повтор с тем же ключом и телом — 201 и тот же `id`; тот же ключ с другой ценой — 422; через
три секунды в списке ровно один заказ в статусе `CONFIRMED` и баланс 70, то есть списание
одно; новый ключ — новый заказ.

## Удаление

```bash
kubectl delete -f k8s/
helm uninstall strimzi --namespace kafka && kubectl delete namespace kafka
helm uninstall cnpg --namespace cnpg-system && kubectl delete namespace cnpg-system
helm uninstall nginx --namespace m && kubectl delete namespace m
```
