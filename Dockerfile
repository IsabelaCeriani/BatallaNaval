FROM maven:3.6.3-openjdk-14 AS MAVEN_BUILD
MAINTAINER Brian Hannaway
COPY pom.xml /build/
COPY src /build/src/
WORKDIR /build/
RUN mvn package
FROM openjdk:14-alpine
WORKDIR /app
EXPOSE 8080
COPY --from=MAVEN_BUILD /target/deploy_render-1.0.0.jar app.jar /app/
ENTRYPOINT ["java", "-jar", "eventIt-0.0.1-SNAPSHOT.jar"]