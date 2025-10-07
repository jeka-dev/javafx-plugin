![_dev.sample.Build Status](https://github.com/jeka-dev/javafx-plugin/actions/workflows/main.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/dev.jeka/javafx-plugin)](https://search.maven.org/search?q=g:%22dev.jeka%22%20AND%20a:%22openapi-plugin%22)

# JavaFX plugin for JeKa

Build and run JavaFX application from source.

## Features

- Declare simple JavaFX dependencies, the plugin will setup native dependencies for you.
- Handle JavaFX module management to let JavaFX execute from source code
- Run everywhere without needing to package the application

## How to Use

Import the plugin from maven and activate in your *jeka.properties* file.
Mentioning the JavaFX version is optional, as per default, it will use the same version that the project or running JDK.

```properties
jeka.classpath=dev.jeka:javafx-plugin:0.11.55-0
@javafx=on

## Optional
@javafx.version=25
```

Declare needed JavaFX dependencies in the *compile-only* section of your *dependencies.txt* file.

```ini
[compile-only]
org.openjfx:javafx-controls
org.openjfx:javafx-graphics
org.openjfx:javafx-fxml
```

From here, you can now code your application and execute it using one of the classic command:
- `jeka project: runMain`
- `jeka project: runJar`
- `jeka -p`

## Install your app anywhere

By pushing to a git repository, your application can be runnable or installable anywhere, without extra effort.
Even on a machine where no Java nor JavaFX is installed.
- `jeka app: install repo=your_app_git_repo_url`

## Run in the IDE

When running on IDE, you must supply the JavaFX modules as JVM options in your launcher.
To grab those options, execute `jeka javafx: jvmOptions` and copy-paste the result into your IDE launcher configuration.

## Example

This repository contains a basic JavaFX application example in *src/main/Java*


