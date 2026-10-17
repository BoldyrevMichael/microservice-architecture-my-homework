# user-service: метрики, дашборд Grafana и алерты

Сервис пользователей из hw4 отдаёт метрики в формате Prometheus (Spring Boot Actuator +
Micrometer), ingress-nginx и PostgreSQL отдают свои. Всё это собирает Prometheus из
kube-prometheus-stack, а Grafana показывает на одном дашборде: latency по квантилям, RPS и
ошибки 5xx по методам API и по сервису целиком со стороны ingress-nginx, CPU и память подов,
работу базы. Два алерта Grafana следят за долей 5xx и p95 времени ответа.

## Состав

- `src/` — user-service из hw4 плюс actuator и `micrometer-registry-prometheus`; в
  `application.yml` открыт `/actuator/prometheus` и включена гистограмма
  `http.server.requests`, без неё квантили в PromQL не посчитать.
- `Dockerfile` — образ `boldyrevma/user-service:hw5`.
- `k8s/` — манифесты hw4 с новым тегом образа и `32-servicemonitor.yaml`: ServiceMonitor,
  по которому Prometheus находит поды приложения. У Service добавлена метка `app`.
- `helm/user-service/` — тот же чарт с шаблоном ServiceMonitor.
- `db/values.yaml` — кластер PostgreSQL с включённым экспортером метрик CloudNativePG
  (`cluster.monitoring.enabled`), он создаёт PodMonitor; `db/operator-values.yaml` — оператор.
- `monitoring/kube-prometheus-stack-values.yaml` — Prometheus, Grafana, kube-state-metrics,
  node-exporter с лимитами; Alertmanager и скрейп control plane выключены; Prometheus берёт
  ServiceMonitor и PodMonitor из всех пространств имён; Grafana с анонимным доступом на
  чтение и sidecar-ами, которые подхватывают дашборды и алерты из ConfigMap.
- `monitoring/ingress-nginx-metrics-values.yaml` — метрики контроллера и его ServiceMonitor,
  добавляется к `ingress-nginx/values.yaml`.
- `grafana/user-service-dashboard.json` — дашборд; `grafana/alert-rules.yaml` — правила
  алертов в формате provisioning Grafana.
- `screenshots/` — дашборд и алерты во время нагрузки.
- `postman/` (коллекция и вывод newman), `kind/`, `ingress-nginx/` — как в hw4.

## Дашборд

Четыре ряда панелей, источник данных Prometheus:

| Ряд | Панели | Метрики |
|---|---|---|
| user-service по методам API | RPS, 5xx в секунду, latency p50 / p95 / p99 / max | `http_server_requests_seconds_*` с разбивкой `method` + `uri` (`/user`, `/user/{id}`, `/health`) |
| ingress-nginx, сервис целиком | RPS, 5xx в секунду, latency p50 / p95 / p99 / max | `nginx_ingress_controller_requests`, `nginx_ingress_controller_request_duration_seconds_bucket` по ingress `user-service` |
| Поды user-service | CPU в ядрах и память working set по подам, линия лимита | cAdvisor через kubelet, лимиты из kube-state-metrics |
| PostgreSQL user-db | соединения приложения по состоянию, транзакции/с, попадание в кэш, строки/с, размер базы | `cnpg_*` встроенного экспортера CloudNativePG |

Квантили считаются `histogram_quantile` по корзинам за минуту. У приложения max берётся из
`http_server_requests_seconds_max`, у ingress-nginx отдельной max-метрики нет, поэтому max
показан как `histogram_quantile(1, ...)`, то есть верхняя граница самой большой непустой корзины.

## Алерты

Оба правила в `grafana/alert-rules.yaml`, считаются каждые 30 секунд по окну 2 минуты и
срабатывают, если условие держится минуту:

- **доля ответов 5xx выше 5 %** — `sum(rate(..._count{status=~"5.."})) / sum(rate(..._count))`,
  severity critical;
- **p95 времени ответа выше 500 мс** — `histogram_quantile(0.95, ...)` по всем методам
  кроме actuator, severity warning.

Уведомления уходят в контакт по умолчанию Grafana; почтовый сервер в стенде не настроен,
состояние алертов видно на странице Alerting → Alert rules.

## Сборка образа

```bash
./gradlew build
docker build --platform linux/amd64 -t boldyrevma/user-service:hw5 .
docker push boldyrevma/user-service:hw5
```

## Кластер

```bash
minikube start --driver=docker --ports=127.0.0.1:80:80,127.0.0.1:443:443
# или: kind create cluster --config kind/cluster.yaml
```

В `/etc/hosts` добавляется строка `127.0.0.1 arch.homework`.

## Установка

Мониторинг первым: он приносит CRD ServiceMonitor и PodMonitor, на которые опираются
остальные шаги.

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx/
helm repo add cnpg https://cloudnative-pg.github.io/charts
helm repo update

helm install monitoring prometheus-community/kube-prometheus-stack --namespace monitoring --create-namespace \
  -f monitoring/kube-prometheus-stack-values.yaml
kubectl -n monitoring rollout status deployment/monitoring-grafana
```

Ingress-контроллер с метриками (при уже установленном контроллере та же команда с
`helm upgrade`):

```bash
kubectl create namespace m
helm install nginx ingress-nginx/ingress-nginx --namespace m \
  -f ingress-nginx/values.yaml -f monitoring/ingress-nginx-metrics-values.yaml
kubectl -n m rollout status daemonset/nginx-ingress-nginx-controller
```

База с экспортером и приложение, как в hw4:

```bash
kubectl apply -f k8s/00-namespace.yaml -f k8s/11-secret.yaml
helm install cnpg cnpg/cloudnative-pg --namespace cnpg-system --create-namespace -f db/operator-values.yaml
kubectl -n cnpg-system rollout status deployment/cnpg-cloudnative-pg
helm install user-db cnpg/cluster --namespace msa -f db/values.yaml
kubectl -n msa wait cluster/user-db --for=condition=Ready --timeout=300s

kubectl apply -f k8s/
kubectl -n msa wait job/user-service-migrate --for=condition=complete --timeout=180s
kubectl -n msa rollout status deployment/user-service
```

Дашборд и алерты попадают в Grafana через ConfigMap с метками, которые ищут sidecar-ы:

```bash
kubectl -n monitoring create configmap user-service-dashboard --from-file=grafana/user-service-dashboard.json
kubectl -n monitoring label configmap user-service-dashboard grafana_dashboard=1
kubectl -n monitoring create configmap user-service-alerts --from-file=grafana/alert-rules.yaml
kubectl -n monitoring label configmap user-service-alerts grafana_alert=1
```

Через полминуты дашборд `user-service` и папка алертов `user-service` появляются в Grafana.

## Доступ к Grafana и Prometheus

```bash
kubectl -n monitoring port-forward svc/monitoring-grafana 3000:80
kubectl -n monitoring port-forward svc/monitoring-kube-prometheus-prometheus 9090:9090
```

Grafana: http://localhost:3000, дашборд открывается без входа (анонимный доступ на чтение),
правка и алерты под `admin` / `grafana-hw5`. Prometheus: http://localhost:9090, цели
`user-service`, `nginx-ingress-nginx-controller-metrics` и `user-db-cluster-podmonitor` в
Status → Targets.

## Нагрузка

Скриншоты в `screenshots/` сняты серверным рендером Grafana (`/render/d/user-service/...`)
во время такой нагрузки: чтение одного пользователя (8 соединений, до 40 запросов/с в каждом,
фактически около 350/с), обновление (2 соединения по 8/с), запросы несуществующего id
(2 соединения по 10/с, ответы 404) и цикл создать-удалить. Чтобы показать
ошибки и алерты, на второй минуте экземпляр PostgreSQL на четыре минуты отключён штатным
фенсингом CloudNativePG:

```bash
kubectl -n msa annotate cluster user-db cnpg.io/fencedInstances='["*"]' --overwrite   # включить
kubectl -n msa annotate cluster user-db cnpg.io/fencedInstances-                       # выключить
```

Пока база отключена, запросы висят на ожидании соединения из пула (30 секунд) и
завершаются 500, поэтому растут и latency, и доля 5xx. Алерты считаются по окну в две
минуты, поэтому в Pending они переходят примерно через две с половиной минуты после отказа,
а в Firing ещё через минуту. Простое удаление пода базы (`kubectl delete pod user-db-1`) такого эффекта не даёт:
CloudNativePG останавливает Postgres мягко и поднимает его обратно за секунды.

`dashboard-alerting.png` — момент срабатывания алертов, `dashboard-final.png` — после
окончания нагрузки с восстановленной базой.

Основная часть нагрузки одной командой, `hey` из Docker; контейнер не видит `/etc/hosts`
хоста, поэтому имя передаётся флагом:

```bash
ID=$(curl -s -X POST http://arch.homework/user -H 'Content-Type: application/json' \
  -d '{"username":"load1","firstName":"L","lastName":"T","email":"l@t.com","phone":"+70000000001"}' | jq .id)
docker run --rm --add-host arch.homework:host-gateway williamyeh/hey -z 7m -q 40 -c 8 http://arch.homework/user/$ID
```

## Удаление

```bash
kubectl -n monitoring delete configmap user-service-dashboard user-service-alerts
helm uninstall user-db --namespace msa
kubectl delete -f k8s/
helm uninstall cnpg --namespace cnpg-system
kubectl delete namespace cnpg-system
helm uninstall monitoring --namespace monitoring
kubectl delete namespace monitoring
kubectl delete crd -l app.kubernetes.io/name=kube-prometheus-stack   # CRD Prometheus Operator helm не удаляет
helm uninstall nginx --namespace m && kubectl delete namespace m
```

Кластер целиком: `minikube delete` или `kind delete cluster --name msa`. Образ локально:
`docker rmi boldyrevma/user-service:hw5`, тег в Docker Hub удаляется на странице репозитория
(Tags → Delete).
