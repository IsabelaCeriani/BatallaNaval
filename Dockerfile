FROM openjdk:17-jdk-slim AS build

# Install curl and unzip
RUN apt-get update && \
    apt-get install -y curl unzip && \
    rm -rf /var/lib/apt/lists/*

# Install Gradle
WORKDIR /opt
RUN curl -L https://services.gradle.org/distributions/gradle-7.6.3-bin.zip -o gradle-7.6.3-bin.zip && \
    unzip gradle-7.6.3-bin.zip && \
    rm gradle-7.6.3-bin.zip

# Copy project files and build
WORKDIR /app
COPY . .
RUN /opt/gradle-7.6.3/bin/gradle build


# Stage 2: Runtime Stage
FROM openjdk:17-jdk-slim
EXPOSE 8080
RUN mkdir /app

# Copy built artifact from the build stage
COPY --from=build /app/build/libs/*.jar /app/batalla_naval-api.jar

# Set the entrypoint command
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=production", "/app/batalla_naval-api.jar"]
