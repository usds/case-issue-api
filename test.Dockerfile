FROM gradle:5.6-jdk8
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle assemble

ENTRYPOINT ["./gradlew", "test"]
