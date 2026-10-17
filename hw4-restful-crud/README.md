# user-service: RESTful CRUD пользователей

Сервис на Spring Boot хранит пользователей в PostgreSQL и отдаёт их через REST API
(`/user`, поля `id`, `username`, `firstName`, `lastName`, `email`, `phone`). База
разворачивается оператором CloudNativePG из helm, настройки приложения лежат в ConfigMap,
доступы к базе в Secret, первоначальная миграция схемы выполняется отдельной Job. Снаружи
сервис доступен через ingress-nginx на хосте `arch.homework`.

## Состав

- `src/` — приложение: `controller` (REST и `/health`), `service`, `repository`, `entity`,
  `dto`, `config` (фильтр хвостового слеша); миграция Flyway в `resources/db/migration`.
- `Dockerfile` — двухстадийная сборка, образ `boldyrevma/user-service:hw4`.
- `db/values.yaml` — кластер PostgreSQL для chart `cnpg/cluster`; `db/operator-values.yaml` —
  лимиты оператора.
- `k8s/` — манифесты по порядку применения: namespace, ConfigMap, Secret, Job миграций,
  Deployment, Service, Ingress. Job и Deployment начинаются с initContainer, который ждёт
  порт базы, поэтому JVM стартует один раз, уже с доступной базой.
- `helm/user-service/` — те же объекты в виде helm-чарта.
- `ingress-nginx/values.yaml` — значения для установки ingress-nginx (DaemonSet с hostPort 80/443);
  `kind/cluster.yaml` — кластер kind с пробросом портов 80/443 на localhost.
- `postman/user-service.postman_collection.json` — сценарий создать → прочитать → изменить →
  удалить, `baseUrl` = `arch.homework`; `postman/newman-output.txt` — вывод прогона newman.

## API

| Метод | Путь | Ответ |
|---|---|---|
| `POST /user` | тело `{username, firstName, lastName, email, phone}` | 201 и созданный пользователь с `id` |
| `GET /user/{id}` | | 200 и пользователь |
| `PUT /user/{id}` | тело `{firstName, lastName, email, phone}` | 200 и обновлённый пользователь |
| `DELETE /user/{id}` | | 204 |
| `GET /health` | | 200 `{"status":"OK"}` |

Ошибки приходят в формате `{"code": 404, "message": "user 42 not found"}`: 404 для
неизвестного `id`, 400 для невалидного тела (пустой `username`, кривой `email`), 409 для
повторного `username`. Хвостовой слеш в пути (`/user/1/`) не мешает.

## Сборка образа

```bash
./gradlew build
docker build --platform linux/amd64 -t boldyrevma/user-service:hw4 .
docker push boldyrevma/user-service:hw4
```

## Кластер

minikube с пробросом портов 80 и 443 на localhost:

```bash
minikube start --driver=docker --ports=127.0.0.1:80:80,127.0.0.1:443:443
```

или kind с той же настройкой портов из `kind/cluster.yaml`:

```bash
kind create cluster --config kind/cluster.yaml
```

В `/etc/hosts` добавляется строка `127.0.0.1 arch.homework` (с драйвером-виртуалкой minikube
вместо 127.0.0.1 подставляется `minikube ip`).

Ingress-контроллер в пространстве имён `m`:

```bash
kubectl create namespace m
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx/
helm repo update
helm install nginx ingress-nginx/ingress-nginx --namespace m -f ingress-nginx/values.yaml
kubectl -n m rollout status daemonset/nginx-ingress-nginx-controller
```

## Установка

Пространство имён и Secret с учётной записью базы нужны до создания базы, потому что
CloudNativePG берёт из этого Secret пароль владельца:

```bash
kubectl apply -f k8s/00-namespace.yaml -f k8s/11-secret.yaml
```

База данных. Оператор ставится один раз в своё пространство имён, затем кластер PostgreSQL
из values-файла:

```bash
helm repo add cnpg https://cloudnative-pg.github.io/charts
helm repo update
helm install cnpg cnpg/cloudnative-pg --namespace cnpg-system --create-namespace -f db/operator-values.yaml
kubectl -n cnpg-system rollout status deployment/cnpg-cloudnative-pg

helm install user-db cnpg/cluster --namespace msa -f db/values.yaml
kubectl -n msa wait cluster/user-db --for=condition=Ready --timeout=300s
```

Первоначальные миграции:

```bash
kubectl apply -f k8s/10-configmap.yaml -f k8s/20-migration-job.yaml
kubectl -n msa wait job/user-service-migrate --for=condition=complete --timeout=180s
```

Приложение. Одна команда на весь каталог применяет манифесты в порядке номеров, уже созданные
объекты остаются без изменений:

```bash
kubectl apply -f k8s/
kubectl -n msa rollout status deployment/user-service
```

Повторный запуск миграций (Job неизменяема): `kubectl -n msa delete job user-service-migrate`
и снова `kubectl apply -f k8s/20-migration-job.yaml`.

## Проверка

```bash
curl -i -X POST http://arch.homework/user -H 'Content-Type: application/json' \
  -d '{"username":"johndoe589","firstName":"John","lastName":"Doe","email":"bestjohn@doe.com","phone":"+71002003040"}'
curl -i http://arch.homework/user/1
newman run postman/user-service.postman_collection.json
```

Коллекция создаёт пользователя со случайным `username`, читает его, меняет профиль, проверяет
изменения, удаляет и убеждается, что после удаления приходит 404.

## Установка helm-чартом

Вместо `k8s/` можно поставить приложение чартом: он создаёт те же ConfigMap, Secret, Job
миграций, Deployment, Service и Ingress. Порядок команд: оператор, чарт приложения, затем
база. Ничем, кроме последовательности команд, он не закреплён, но каждый шаг опирается на
предыдущий: без оператора объект `Cluster` никто не обрабатывает (а если его CRD остались
от прошлой установки, `helm install` базы пройдёт, но база так и не появится); Secret
`user-db-credentials` с паролем владельца базы создаёт чарт приложения, и база ждёт его
появления; Job миграций и поды приложения ждут порт базы в initContainer и стартуют, как
только она готова, поэтому их можно создать раньше базы.

```bash
helm install cnpg cnpg/cloudnative-pg --namespace cnpg-system --create-namespace -f db/operator-values.yaml
kubectl -n cnpg-system rollout status deployment/cnpg-cloudnative-pg
helm install user-service helm/user-service --namespace msa --create-namespace
helm install user-db cnpg/cluster --namespace msa -f db/values.yaml
kubectl -n msa wait job/user-service-migrate-1 --for=condition=complete --timeout=300s
kubectl -n msa rollout status deployment/user-service
```

Параметры (образ, число реплик, адрес и учётная запись базы, хост) в `helm/user-service/values.yaml`.
При каждом `helm upgrade` создаётся новая Job миграций с номером ревизии.

## Удаление

Сначала база, потом приложение: `kubectl delete -f k8s/` удаляет и пространство имён `msa`,
а вместе с ним том базы и запись helm о релизе `user-db`, после чего `helm uninstall` его
уже не найдёт.

```bash
helm uninstall user-db --namespace msa
kubectl delete -f k8s/        # при установке чартом: helm uninstall user-service --namespace msa && kubectl delete namespace msa
helm uninstall cnpg --namespace cnpg-system
kubectl delete namespace cnpg-system
```

Ingress-контроллер: `helm uninstall nginx --namespace m && kubectl delete namespace m`.
Кластер целиком: `minikube delete` или `kind delete cluster --name msa`.

Образ локально: `docker rmi boldyrevma/user-service:hw4`. Из Docker Hub тег удаляется на
странице репозитория (Tags → Delete) или через API:

```bash
TOKEN=$(curl -s -H 'Content-Type: application/json' -X POST \
  -d '{"username":"boldyrevma","password":"<пароль или access token>"}' \
  https://hub.docker.com/v2/users/login/ | jq -r .token)
curl -X DELETE -H "Authorization: JWT $TOKEN" \
  https://hub.docker.com/v2/repositories/boldyrevma/user-service/tags/hw4/
```
