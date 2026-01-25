# Use official OpenJDK image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy gradle wrapper + build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

# Give execution permissions
RUN chmod +x ./gradlew

# Build the project
RUN ./gradlew clean build -x test

# Expose port
EXPOSE 8080

# Run Spring Boot jar
ENTRYPOINT ["java","-jar","build/libs/yourapp-0.0.1-SNAPSHOT.jar"]