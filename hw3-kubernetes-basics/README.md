# health-service в Kubernetes

Сервис из hw2 (образ `boldyrevma/health-service:hw2`, `GET /health` → `{"status":"OK"}`)
разворачивается в Kubernetes: Deployment в две реплики с пробами, Service и Ingress на хосте
`arch.homework`. Все манифесты лежат в `k8s/` и применяются одной командой.

## Состав

- `k8s/00-namespace.yaml` — пространство имён `msa`.
- `k8s/10-deployment.yaml` — 2 реплики, образ с Docker Hub, readiness и liveness по `/health`,
  лимиты ресурсов, запуск без root.
- `k8s/20-service.yaml` — ClusterIP на порт 8000.
- `k8s/30-ingress.yaml` — два маршрута на хосте `arch.homework`: `/health` и
  `/otusapp/boldyrev/<путь>`, который переписывается в `/<путь>`, например
  `/otusapp/boldyrev/health` → `/health`. Оба пути заданы regex-ом с двумя группами, потому что
  `rewrite-target: /$2` действует на весь Ingress.
- `ingress-nginx/values.yaml` — значения для установки ingress-nginx (DaemonSet с hostPort 80/443).
- `kind/cluster.yaml` — конфигурация кластера kind с пробросом портов 80/443 на localhost.
- `postman/health-service.postman_collection.json` — коллекция с проверками обоих адресов,
  переменная `baseUrl` = `arch.homework`.

## Кластер

### minikube

```bash
minikube start --driver=docker --ports=127.0.0.1:80:80,127.0.0.1:443:443
```

С драйвером docker порты узла пробрасываются на localhost, поэтому в `/etc/hosts` добавляется
строка `127.0.0.1 arch.homework`. С драйвером-виртуалкой (hyperkit, qemu, virtualbox) порт
80 узла доступен по адресу `minikube ip`, его и нужно вписать в `/etc/hosts` вместо 127.0.0.1.

### kind

```bash
kind create cluster --config kind/cluster.yaml
```

В `/etc/hosts` добавляется строка `127.0.0.1 arch.homework`.

## Установка

Ingress-контроллер в пространстве имён `m`:

```bash
kubectl create namespace m
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx/
helm repo update
helm install nginx ingress-nginx/ingress-nginx --namespace m -f ingress-nginx/values.yaml
kubectl -n m rollout status daemonset/nginx-ingress-nginx-controller
```

Приложение:

```bash
kubectl apply -f k8s/
kubectl -n msa rollout status deployment/health-service
```

## Проверка

```bash
curl http://arch.homework/health
curl http://arch.homework/otusapp/boldyrev/health
```

Оба запроса возвращают `{"status":"OK"}`. Та же проверка коллекцией Postman:

```bash
newman run postman/health-service.postman_collection.json
```

## Удаление

```bash
kubectl delete -f k8s/
helm uninstall nginx --namespace m
kubectl delete namespace m
```

Кластер целиком: `minikube delete` (удаляет только профиль `minikube`, созданный выше,
другие профили не трогает) или `kind delete cluster --name msa`. Строку `arch.homework`
из `/etc/hosts` убрать вручную.
