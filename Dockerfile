# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jdk-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app.jar", "--server.port=10000"]