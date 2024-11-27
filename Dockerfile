FROM maven:3.9.9-ibm-semeru-21-jammy AS build

WORKDIR /app

COPY . .

RUN mvn clean install -DskipTests

FROM openjdk:21-jdk-slim

EXPOSE 8080

COPY --from=build /app/target/organiza-0.0.1-SNAPSHOT.jar /app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]