# Use the latest Ubuntu image as the build environment
FROM ubuntu:latest AS build

# Update package lists
RUN apt-get update && apt-get install -y openjdk-21-jdk maven

# Set the working directory in the container
WORKDIR /app

# Copy the source code into the container
COPY . .

# Build the application
RUN mvn clean install

# Use OpenJDK 21 slim image for the final stage
FROM openjdk:21-jdk-slim

# Expose the application's port
EXPOSE 8080

# Copy the built JAR file from the build stage
COPY --from=build /app/target/api-0.0.1-SNAPSHOT.jar app.jar

# Define the entry point
ENTRYPOINT ["java", "-jar", "app.jar"]
