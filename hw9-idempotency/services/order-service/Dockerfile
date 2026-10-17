FROM --platform=$BUILDPLATFORM eclipse-temurin:25-jdk AS build
WORKDIR /src
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
COPY src src
RUN --mount=type=cache,target=/root/.gradle chmod +x gradlew && ./gradlew --no-daemon -q bootJar

FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S -g 10001 app && adduser -S -u 10001 -G app app
USER 10001:10001
WORKDIR /app
COPY --from=build /src/build/libs/order-service-*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "app.jar"]
