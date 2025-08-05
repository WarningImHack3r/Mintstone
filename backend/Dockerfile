FROM eclipse-temurin:21.0.8_9-jdk-jammy AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

RUN ./gradlew clean build -x test

FROM eclipse-temurin:21.0.8_9-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/mintstone-backend.jar app.jar
EXPOSE 8080
CMD [ "java", "--enable-preview", "-jar", "app.jar" ]
