# Multi-stage build
FROM maven:3.8.6-openjdk-11 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml first for better caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Copy the JAR file from builder stage (using wildcard to handle any version)
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 9000

# JVM optimizations for containers
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

