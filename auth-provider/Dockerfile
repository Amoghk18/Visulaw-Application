# Use a lightweight OpenJDK image
FROM eclipse-temurin:21-jdk-jammy

# Set working directory
WORKDIR /app

# Copy built jar from host into container
COPY target/auth-provider-0.0.1-SNAPSHOT.jar app.jar

# Expose service port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]