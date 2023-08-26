FROM eclipse-temurin:20-jdk-alpine AS build-back

WORKDIR /app/back

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

RUN ./gradlew clean build -x test

FROM eclipse-temurin:20-jre-alpine
WORKDIR /app/back
COPY --from=build-back /app/back/build/libs/mintstone-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "--enable-preview", "-jar", "app.jar"]
