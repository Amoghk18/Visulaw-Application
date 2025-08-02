# Use a lightweight OpenJDK image
FROM eclipse-temurin:21-jdk-jammy

# Set working directory
WORKDIR /app

# Install curl to support wait-for-it.sh
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the wait-for-it script (you'll create this next)
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

# Copy built jar from host into container
COPY target/legal-service-0.0.1-SNAPSHOT.jar app.jar

# Expose service port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["/wait-for-it.sh", "redis:6379", "--", "java", "-jar", "app.jar"]