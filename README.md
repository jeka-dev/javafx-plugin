![_dev.sample.Build Status](https://github.com/jeka-dev/javafx-plugin/actions/workflows/main.yml/badge.svg)
[![Maven Central](https://img.shields.io/maven-central/v/dev.jeka/javafx-plugin)](https://search.maven.org/search?q=g:%22dev.jeka%22%20AND%20a:%22openapi-plugin%22)

# JavaFX plugin for JeKa

Build and run a JavaFX application from the source. 
This plugin adapts the `Project` KBean to simplify the JavaFX setup process.

## Features

- Use basic JavaFX dependencies; the plugin automatically manages native setup for you.
- Manage JavaFX modules/kit behind the scene.
- Run JavaFX apps on any platform directly from a Git repo, no packaging needed.

## How to Use

This plugin requires JeKa 0.11.55 or greater.

Import the plugin and activate it in your *jeka.properties* file.  

Specifying the JavaFX version is optional. By default, it uses the version of the project's JDK or the running JDK.

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
- `jeka -p` (directly run the app bypassing the JeKa engine)

## Install your app anywhere

Pushing to a Git repository lets your app run or install anywhere, with no extra work— even on machines without Java or JavaFX.

To install and run a JavaFX app stored in a Git repo:
- `jeka app: install repo=your_app_git_repo_url`
- `jeka app: install repo=your_app_git_repo_url#0.1.0` (to specify a Git tag)

## Run in the IDE

When running on IDE, you must supply the JavaFX modules as JVM options in your launcher.
To grab those options, execute `jeka javafx: jvmOptions` and copy-paste the result into your IDE launcher configuration.

## Example

- https://github.com/djeang/devtools A graphical toolbox for developers, ported from Maven to Jeka.
