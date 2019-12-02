FROM gradle:5.4.1-jdk8
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle gradle ./gradle
COPY --chown=gradle:gradle ./*.gradle ./
RUN gradle --info dependencies
COPY --chown=gradle:gradle ./.git ./.git
COPY --chown=gradle:gradle ./src ./src

RUN ls -la
RUN gradle classes testClasses assemble

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/case_issues

ENTRYPOINT ["gradle", "--info", "--no-daemon"]
CMD ["tasks"]
