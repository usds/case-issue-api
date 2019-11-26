# Case Issue API

This is the persistence API for tracking, triaging, and managing cases that
have fallen out of the normal processing pipeline. It is intended to be used with
the [Case Issue Navigator](../case-issue-navigator) browser application, but presents
an HTTP/JSON API that can be consumed by other clients if needed.

# Development

## Prerequisites
- [Install Java Development Kit (JDK)](https://www.oracle.com/technetwork/java/javase/downloads/index.html)

## Basic set-up

```bash
docker-compose build
docker-compose up
docker build -f setup.Dockerfile -t setup . && docker run -i -t setup
```

This project uses the [gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) to avoid build tool versioning issues. Setting it up is as simple as typing `./gradlew build` in the project directory. Alternatively, you can set it up as a new Gradle project in Eclipse/Spring Tool Suite, or as a new Java project in your IDE of choice. (If you find yourself frequently typing `gradle test` instead of `./gradlew test`, you may wish to `alias gradle=./gradlew` to save yourself some aggravation.)

## Tests and checks

You can run unit tests as a group using:

```bash
docker-compose -f docker-compose.yml -f docker-compose.test.yml build
docker-compose -f docker-compose.yml -f docker-compose.test.yml up
open ./test-reports/tests/test/index.html
```

or run all checks using:

```bash
docker-compose -f docker-compose.yml -f docker-compose.check.yml build
docker-compose -f docker-compose.yml -f docker-compose.check.yml up --exit-code-from api
open ./test-reports/tests/test/index.html
```

Check will:

- run `checkstyle` on both main and test classes
- run all tests
- run JaCoCo to check for adequate test coverage

To configure your IDE to report style violations, use the checkstyle configuration in [config/checkstyle/checkstyle.xml].

## Starting the Application

1. Using the command line

    ```bash
    docker-compose build
    docker-compose up
    ```

2. Using Spring Tool Suite (Eclipse)
   - Run as: Spring Boot Application
   - Edit the Run Configuration to set the profile to `dev`

## Configuring the application

This is a Spring Boot project, so it picks up most of its configuration from files in [src/main/resources].
The main files to be aware of are:

* application.yml — this is the main properties file, where we put configuration entries that
  should not be changed from environment to environment.
* hibernate.properties — this is the main configuration file for the persistence layer. It should
  also not need to be changed frequently.
* application-dev.yml — this contains additional configuration for running in local development.
  It will be loaded if the `dev` profile is active (which it should be), and it will also automatically
  activate the `local` profile.
* application-local.yml — this file is not in version control: you can use it to set additional properties
  (logging configuration, sample data file locations, and so forth) that are local to your development
  setup, rather than being common characteristics of everybody's local development environments.

### Customizable Configuration Elements

Several custom sections can be added to the application properties:

- `web-customization` customizes the server configuration
  - `cors-origins` is a list of allowed origins for cross-origin resource sharing
  - `users` is a list of test users (for use in development environments), for testing with various
    authorization levels
- `oauth-user-config` customizes the way that OAuth2/OIDC user ID tokens are translated into local
  users (with local permissions).
  - `name-path` (optionally) provides a path to the value in the user's `attributes` map where we can find
    the actual durable user ID (if the IDP's notion of durable user ID does not map to the application's).
  - `authority-paths` (optionally in some sense, but likely necessarily) configures how to translate the
    user's `attributes` to internal authorities for this application.

## Loading Sample Data

- `setup.py` contains configuration for loading fake data into your development environment

If you have successfully started the application with the `dev` profile and don't want to mess
with the profiles and restart just to see some data, this command should get you started:

   curl -i -X PUT -u service:service -HContent-type:text/csv --data-binary '@sample_data/cases.csv' localhost:8080/api/cases/OTHER/WEIRD/SILLY

If the data uses a non-default key for the receipt number or case creation date, or a non-standard
format for the creation date, you can save upload configurations as a dictionary in the application
properties under `web-customization.data-formats` like this:

    web-customization:
        data-formats:
            my-format:
                receipt-number-key: caseIdString
                creation-date-key: case-was-created-at
                creation-date-format: "EEE MMM dd yyyy"

Then refer to that format in the upload using the `uploadSchema` request parameter:

   curl -i -X PUT -u service:service -HContent-type:text/csv --data-binary '@sample_data/cases.csv' localhost:8080/api/cases/OTHER/WEIRD/SILLY?uploadScehma=my-format

## Updating a dependency

1. Update the dependency in [build.gradle](../build.gradle#L21)
2. Update the lock file with `./gradlew dependencies --write-locks`
3. Rebuild the project with `./gradlew build`

# Running the Application Locally

Once you have started your local server, this information will help you get started experimenting with it.

## Exploring the API

The API is divided into two distinct sections: one under the `resources` tree, which is
intended for administrative and testing purposes, and the main customer-facing API under
`api`, which is intended to meet all the day-to-day-needs of normal users.

The `resources` API uses the HAL browser, which can be found at http://localhost:8080/resources.

The main API has an OpenAPI specification, and can be viewed at http://localhost:8080/swagger-ui.html.

## Authentication and Authorization

By default (unless you override this in `application-local.yml`) there are three users available
when the `dev` profile is activated: `admin`, `boring_user` and `service`. All of them have their
username as their password, and can be used either through the default form-based login page
(at `/login`) or with HTTP Basic Authentication (e.g. `curl -u admin:admin ...`). Their permissions are:

* `admin`: all `/resource` operations, and all `/api` operations except for the PUT endpoint that
  updates the issue list.
* `boring_user`: all `/api` operations except for the PUT endpoint that updates the issue list.
* `service`: _only_ the PUT endpoint that updates the issue list.

All unsafe operations require a CSRF token tied to the user's current session. (Arguably, this
makes HTTP Basic Authentication somewhat useless even in the development environment—patches
are welcome!)

To obtain the CSRF token for your current session, you should send a GET to `/csrf`, which will return
the token and the acceptable ways of submitting it on unsafe requests (a header and a form parameter,
of which only the header is likely useful in this context).

The CSRF token will be automatically read and used by the Swagger UI at `/swagger-ui.html`, but not by
the HAL browser at `/resources`. However, the HAL browser has an entry field for "Custom Request Headers"
at the  top of the screen: simply paste your desired CSRF header (probably something along the lines
of `X-CSRF-TOKEN: abcd-ef01234-567890`) into that text area before making an unsafe request.

# Deployment

As a standalone Spring Boot jar, this application is intended to be deployed in a platform-as-a-service
(PaaS) environment. Properties files that are included in the source tree should be included in the
runnable jar file, but local properties (such as the connection information for a database) should be
injected separately, either as environment variables or as separate managed properties files that are
added to Spring's property-resolution path at runtime.

## Deployment-specific Properties

### Local Property/YAML Files

Using docker, you can copy global properties files into your image in a separate directory and add
arguments like the following to your ENTRYPOINT invocation:

    --spring.config.additional-location=file:/etc/case-issue-api/config/,file:/usr/local/case-issue-api/config/

(Note that the terminal `/` on each entry is extremely required.)

Equivalently, you can have those files stored in an independent repository, and mount them at the
required location when pods are started in your PaaS system.

### Environment Variables

Any property that can be set in a file should also be able to be set as an environment variable through
your PaaS, and picked up automatically by Spring at runtime (see the
[Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)
for more details).

## Customizing Web Application Behaviors

### Favicon

At build time, you can place a favicon.ico file in src/main/resources/ or src/main/resources/static
to override the default favicon with one that is more specific to your environment. Alternatively,
setting `spring.mvc.favicon.enabled=false` in a properties file (or `SPRING_MVC_FAVICON_ENABLED=false`
in the environment) will cause no favicon to be served.

### Build Information

The build information actuator (at /actuator/info) provides basic information about the application as
built, including the git hash and the version. If you wish to include additional information (for instance,
information about a docker image or the deployment configuration), you can extend this either by modifying
the build.gradle file or by adding a gradle init file with a block like the following:

    afterEvaluate {
        tasks.bootBuildInfo.properties.additional["docker_version"] =
            System.getenv("BUILD_NUMBER") ?: "NO DOCKER VERSION"
    }

Alternatively, if you would package this information in your Dockerfile, you can pass the version
to the docker build command and add an argument to the ENDPOINT directive along these lines:

    --info.docker.version=${VERSION}
