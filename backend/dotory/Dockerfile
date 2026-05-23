FROM gradle:8.14.0-jdk21 AS build

WORKDIR /app

COPY . .

RUN gradle clean bootJar -x test

FROM amazoncorretto:21

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]