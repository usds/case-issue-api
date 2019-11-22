FROM gradle:5.6-jdk8 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
ENV SPRING_PROFILES_ACTIVE=dev
RUN gradle assemble --stacktrace --debug

FROM openjdk:8-jre-slim

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar","/app/app.jar"]
