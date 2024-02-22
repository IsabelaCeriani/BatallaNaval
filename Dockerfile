FROM gradle:7.0-jdk11 AS build

COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle assemble

FROM openjdk:17-oracle
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/posts-api.jar
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=production", "/app/posts-api.jar"]
#ENTRYPOINT ["java","-jar", "/app/posts-api.jar"]