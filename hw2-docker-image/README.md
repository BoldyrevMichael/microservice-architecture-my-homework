# health-service

Минимальный HTTP-сервис в Docker-образе. Отвечает на порту 8000:

```
GET /health/
200 {"status":"OK"}
```

Путь принимается и со слешем на конце, и без него.

## Стек

Java 25, Spring Boot 4.1.1, Gradle 9.8.1 (wrapper). Образ собирается из Dockerfile в два этапа:
сборка jar в `eclipse-temurin:25-jdk`, запуск в `eclipse-temurin:25-jre-alpine` от
непривилегированного пользователя. Стадия сборки выполняется на родной платформе хоста, образ
получается для `linux/amd64`.

## Сборка и запуск

Сборка и тесты:

```bash
./gradlew build
```

Образ для linux/amd64:

```bash
docker build --platform linux/amd64 -t boldyrevma/health-service:hw2 .
```

Запуск с ограничениями ресурсов и проверка:

```bash
docker run -d --name health --memory 256m --cpus 1 -p 8000:8000 boldyrevma/health-service:hw2
curl http://localhost:8000/health/
```

JVM внутри контейнера берёт не больше 75 % памяти контейнера (`JAVA_TOOL_OPTIONS` в Dockerfile).

Остановка и удаление контейнера, затем локального образа:

```bash
docker stop health
docker rm health
docker rmi boldyrevma/health-service:hw2
```

## Публикация

```bash
docker login
docker push boldyrevma/health-service:hw2
```

Образ в Docker Hub: `boldyrevma/health-service`, тег `hw2`.

Удаление из Docker Hub делается на сайте: репозиторий → Tags → выбрать тег → Delete,
либо Settings → Delete repository для репозитория целиком. Через API то же самое:

```bash
TOKEN=$(curl -s -X POST https://hub.docker.com/v2/users/login \
  -H "Content-Type: application/json" \
  -d '{"username": "boldyrevma", "password": "<пароль или access token>"}' | jq -r .token)
curl -X DELETE -H "Authorization: JWT $TOKEN" \
  https://hub.docker.com/v2/repositories/boldyrevma/health-service/tags/hw2/
```
