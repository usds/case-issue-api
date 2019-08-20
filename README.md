# Case Issue API

This is the persistence API for tracking, triaging, and managing cases that
have fallen out of the normal processing pipeline. It is intended to be used with
the [Case Issue Navigator](../case-issue-navigator) browser application, but presents
an HTTP/JSON API that can be consumed by other clients if needed.

# Development

## Prerequisites
- [Install Java Development Kit (JDK)](https://www.oracle.com/technetwork/java/javase/downloads/index.html)

## Basic set-up

This project uses the [gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) to avoid build tool versioning issues. Setting it up is as simple as typing `./gradlew build` in the project directory. Alternatively, you can set it up as a new Gradle project in Eclipse/Spring Tool Suite, or as a new Java project in your IDE of choice. (If you find yourself frequently typing `gradle test` instead of `./gradlew test`, you may wish to `alias gradle=./gradlew` to save yourself some aggravation.)

## Tests and checks

You can run unit tests as a group using `./gradlew test`, or run all checks using `./gradlew check`. This will:

* run `checkstyle` on both main and test classes
* run all tests
* run JaCoCo to check for adequate test coverage

To configure your IDE to report style violations, use the checkstyle configuration in [config/checkstyle/checkstyle.xml].

## Running the Application

1. Using the command line
   
     SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

1. Using Spring Tool Suite (Eclipse)
   * Run as: Spring Boot Application
   * Edit the Run Configuration to set the profile to `dev`

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

## Loading Sample Data

Unless you want to create all the data by hand using the `resources` API (not recommended),
you will want to configure the application to load some sample data. This is most easily done
by adding configuration to `application-local.yml`.

## Exploring the API

The API is divided into two distinct sections: one under the `resources` tree, which is
intended for administrative and testing purposes, and the main customer-facing API under
`api`, which is intended to meet all the day-to-day-needs of normal users.

The `resources` API uses the HAL browser, which can be found at http://localhost:8080/resources.

The main API has an OpenAPI specification, and can be viewed at http://localhost:8080/swagger-ui.html.
