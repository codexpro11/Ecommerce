# Multi-stage build for Spring Boot backend
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY productSite/ ./
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Runtime stage - smaller image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/productSite-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
