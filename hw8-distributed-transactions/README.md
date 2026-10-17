# Создание заказа как распределённая транзакция: сага

Создание заказа проходит через три сервиса: биллинг списывает деньги, склад резервирует
товар, доставка резервирует курьера на слот. Если любой шаг не удался, предыдущие
откатываются: деньги возвращаются на счёт, резерв на складе снимается. Реализовано как
**сага в стиле хореографии**: координатора нет, каждый сервис реагирует на событие
предыдущего шага и публикует своё, компенсации тоже запускаются событиями об отказе.

## Паттерн

![Сага](docs/saga.png)

Снимок заказа (`orderId, userId, price, productId, quantity, slot`) идёт через цепочку тем
Kafka, каждый шаг добавляет в него свой `status` и `reason`:

| Шаг | Слушает | Делает | Публикует |
|---|---|---|---|
| order-service | `POST /orders` | сохраняет заказ `NEW` | `order.created` |
| billing-service | `order.created` | списывает `price` со счёта | `order.payment` `COMPLETED` или `FAILED` |
| warehouse-service | `order.payment` (только `COMPLETED`) | резервирует `quantity` товара | `order.stock` `RESERVED` или `FAILED` |
| delivery-service | `order.stock` (только `RESERVED`) | резервирует курьера на `slot` | `order.delivery` `RESERVED` или `FAILED` |
| order-service | `order.payment`, `order.stock`, `order.delivery` | `PAID` → `RESERVED` → `CONFIRMED`; любой `FAILED` → `CANCELLED` с причиной | `order.completed` |
| notification-service | `order.completed` | письмо «подтверждён» или «отменён» | — |

Компенсации:

| Отказ | Кто откатывает | Как |
|---|---|---|
| `order.payment FAILED` | никто | откатывать нечего, денег не списали |
| `order.stock FAILED` | billing-service | слушает `order.stock`, возвращает `price` на счёт |
| `order.delivery FAILED` | warehouse-service и billing-service | склад снимает резерв по `orderId`, биллинг возвращает `price` |

Почему хореография, а не оркестратор или двухфазный коммит: шагов три и порядок линейный,
каждый сервис знает только свои темы, а Kafka и события уже лежат в основе заказов и
уведомлений; откаты это обычные обработчики событий об отказе. Двухфазный коммит потребовал
бы блокировок в трёх базах и координатора, что для такого потока избыточно. Ключ сообщений во всех темах — `orderId`,
поэтому события одного заказа обрабатываются по порядку. По условию задания сбои сервисов и
повторная доставка не рассматриваются.

## Архитектура

![Схема](docs/architecture.png)

Шесть сервисов в namespace `msa`, у каждого своя база в одном кластере PostgreSQL,
общая Kafka. Клиент ходит ко всем сервисам по HTTP через ingress-nginx: регистрация и вход в
user-service, пополнение и баланс в billing-service, заказы в order-service, остатки на складе,
резервы курьера и письма в своих сервисах. На защищённых путях ingress-nginx проверяет JWT
запросом в user-service (`auth-url`) и подставляет `X-User-Id`. Между собой сервисы по HTTP не
общаются: только события через Kafka. На складе два товара: `1` с остатком 100 и `2` с остатком 0; курьер
один, поэтому слот можно занять один раз.

## API

Все пути, кроме `/register`, `/login` и `/health`, требуют `Authorization: Bearer <token>`.

| Метод | Путь | Ответ |
|---|---|---|
| `POST /register` | `{username, password, firstName, lastName, email, phone}` | 201 пользователь; событие `user.registered` |
| `POST /login` | `{username, password}` | 200 `{token, expiresIn}` |
| `GET /billing/account`, `POST /billing/account/deposit {amount}` | | 200 `{userId, balance}` |
| `POST /orders` | `{price, productId, quantity, slot}` | 201 заказ `NEW`; запуск саги |
| `GET /orders`, `GET /orders/{id}` | | `status`: `NEW`, `PAID`, `RESERVED`, `CONFIRMED`, `CANCELLED` + `reason` |
| `GET /warehouse/products`, `GET /warehouse/products/{id}` | | `{id, name, available}` |
| `GET /delivery/reservations` | | резервы курьера пользователя `{orderId, slot}` |
| `GET /notifications` | | письма пользователя |

## Состав

- `services/<имя>/` — шесть самостоятельных Gradle-проектов: user, billing, order, warehouse,
  delivery, notification; у каждого `controller`, `service`, `repository`, `entity`, `dto`,
  `event` (записи событий, слушатели `@KafkaListener`, издатели через `KafkaTemplate`; снимок
  заказа — запись `OrderEvent`), миграции Flyway, тесты, Dockerfile. Образы
  `boldyrevma/<имя>:hw8`.
- `helm/service/` — общий чарт сервиса, `helm/values/<имя>.yaml` — значения шести сервисов.
- `k8s/` — namespace, Secret, пять `Database`, шесть `KafkaTopic`, по файлу на сервис,
  два Ingress. `kafka/kafka.yaml` — Kafka для Strimzi, `db/` — values CloudNativePG.
- `postman/order-saga.postman_collection.json` — сценарий: успешная сага и три отказа с
  проверкой откатов; `postman/newman-output.txt` — вывод прогона.
- `docs/` — схемы саги и архитектуры.
- `kind/`, `ingress-nginx/` — конфигурация кластера и ingress-контроллера.

## Кластер и ingress-nginx

```bash
minikube start --driver=docker --ports=127.0.0.1:80:80,127.0.0.1:443:443 --memory 6g
# или: kind create cluster --config kind/cluster.yaml
# в /etc/hosts: 127.0.0.1 arch.homework

kubectl create namespace m
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx/
helm repo update
helm install nginx ingress-nginx/ingress-nginx --namespace m -f ingress-nginx/values.yaml
kubectl -n m rollout status daemonset/nginx-ingress-nginx-controller
```

## Установка

Namespace приложения `msa`, операторы в своих: CloudNativePG в `cnpg-system`, Strimzi в
`kafka` (он следит за ресурсами Kafka в `msa`).

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
```

Сервисы, путь 1: манифестами. Одна команда применяет всё по порядку номеров: базы, темы,
миграции, приложения, Ingress; Job и поды дожидаются базы через initContainer:

```bash
kubectl apply -f k8s/
kubectl -n msa wait job --all --for=condition=complete --timeout=300s
kubectl -n msa rollout status deployment --timeout=300s
```

Сервисы, путь 2: общим чартом. Чарт `helm/service` описывает ConfigMap, Job миграций,
Deployment и Service, различия между сервисами (образ, база, секреты) лежат в
`helm/values/<имя>.yaml`, имя релиза становится именем сервиса, на него ссылаются Ingress.
Базы, темы и Ingress остаются манифестами, чарт ставится шесть раз:

```bash
kubectl apply -f k8s/02-databases.yaml -f k8s/03-kafka-topics.yaml
helm install user-service helm/service --namespace msa -f helm/values/user-service.yaml
helm install billing-service helm/service --namespace msa -f helm/values/billing-service.yaml
helm install order-service helm/service --namespace msa -f helm/values/order-service.yaml
helm install warehouse-service helm/service --namespace msa -f helm/values/warehouse-service.yaml
helm install delivery-service helm/service --namespace msa -f helm/values/delivery-service.yaml
helm install notification-service helm/service --namespace msa -f helm/values/notification-service.yaml
kubectl apply -f k8s/40-ingress.yaml -f k8s/41-ingress-auth.yaml
kubectl -n msa wait job --all --for=condition=complete --timeout=300s
kubectl -n msa rollout status deployment --timeout=300s
```

Один чарт вместо шести копий: шаблоны одни, различается только values. В продуктовой
раскладке такие шаблоны выносят в library-чарт, а чарт каждого сервиса держат рядом с его
кодом; здесь это был бы лишний слой. Обновление сервиса — `helm upgrade` с новым тегом
образа, Job миграций создаётся заново с номером ревизии.

Образы собираются и публикуются из каждого `services/<имя>` командами
`./gradlew build`, `docker build --platform linux/amd64 -t boldyrevma/<имя>:hw8 .`,
`docker push boldyrevma/<имя>:hw8`.

## Проверка

```bash
newman run postman/order-saga.postman_collection.json --verbose
```

Пять блоков: регистрация, вход, пополнение на 100 и остаток товара до заказов; успешный
заказ на 60 (товар 1 × 2) → `CONFIRMED`, баланс 40, остаток меньше на 2, резерв курьера на
слот; заказ на 100 → `CANCELLED insufficient funds`, баланс 40; заказ товара 2 → `CANCELLED
out of stock`, деньги вернулись, баланс 40; заказ на тот же слот → `CANCELLED no courier`,
резерв товара снят, деньги вернулись; в уведомлениях одно письмо о подтверждении и три об
отмене. Паузы по три секунды перед чтением дают событиям пройти всю цепочку.

## Удаление

Темы Kafka удаляются первыми, пока жив topic operator: у `KafkaTopic` стоит его финализатор, и
если удалить namespace целиком, темы останутся висеть и не дадут namespace исчезнуть.

```bash
kubectl delete -f k8s/03-kafka-topics.yaml
helm uninstall user-service billing-service order-service warehouse-service delivery-service notification-service -n msa   # только при установке чартом
helm uninstall msa-db --namespace msa
kubectl delete -f k8s/                       # приложения, базы, Kafka, namespace msa с томами
helm uninstall strimzi --namespace kafka && kubectl delete namespace kafka
helm uninstall cnpg --namespace cnpg-system && kubectl delete namespace cnpg-system
helm uninstall nginx --namespace m && kubectl delete namespace m
```

Релиз базы снимается до удаления namespace: вместе с namespace исчезает и запись helm о
релизе; Kafka удаляется вместе с namespace.

Если после удаления namespace `msa` ставить приложение заново, не переустанавливая Strimzi,
сначала повторите `helm upgrade strimzi ...` с теми же параметрами: оператор держит свои
права (RoleBinding) внутри наблюдаемого namespace, они исчезают вместе с ним, и без этого
шага Kafka в новом `msa` не создастся.
