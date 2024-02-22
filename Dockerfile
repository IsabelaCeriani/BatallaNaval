FROM adoptopenjdk/openjdk17:alpine AS builder

# Install Gradle
ENV GRADLE_VERSION=7.0
ENV GRADLE_HOME=/opt/gradle
ENV PATH=$PATH:$GRADLE_HOME/bin
WORKDIR /opt
RUN wget https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip gradle-${GRADLE_VERSION}-bin.zip && \
    rm gradle-${GRADLE_VERSION}-bin.zip

# Set Gradle environment variables
ENV GRADLE_USER_HOME /cache/.gradle

# Copy project files and build
WORKDIR /app
COPY . .
RUN gradle build


FROM openjdk:17-oracle
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/batalla_naval-api.jar
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=production", "/app/batalla_naval-api.jar"]
#ENTRYPOINT ["java","-jar", "/app/posts-api.jar"]