FROM gradle:5.4.1-jdk8
WORKDIR /home/gradle/case-issue-api
RUN apt-get update && apt-get install --yes make
COPY --chown=gradle:gradle gradle ./gradle
COPY --chown=gradle:gradle ./*.gradle ./gradle.properties ./
RUN gradle --info dependencies

COPY --chown=gradle:gradle ./.git ./.git
COPY --chown=gradle:gradle ./config ./config
COPY --chown=gradle:gradle ./src ./src
COPY --chown=gradle:gradle Makefile-testcerts .
RUN echo "spring.profiles.include: db-dockerized" >> src/main/resources/application-autotest-local.yml
RUN gradle classes testClasses assemble

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/case_issues

ENTRYPOINT ["gradle", "--info", "--no-daemon"]
CMD ["tasks"]
