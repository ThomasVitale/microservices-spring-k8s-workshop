# Containerizing Spring Boot

## Learning goals

* Package and run applications as containers
* Configure Cloud Native Buildpacks

## Overview

In this excercise, you will see how to containerize the Book Service application.

* Use the Spring Boot plugin for Gradle to package and run the application as a container image.

* Configure the Cloud Native Buildpacks integration with builder and image name.

## Details

### Containerize Spring Boot

From a Terminal window, run this command to package the Spring Boot application as a container image.

```bash
$ ./gradlew bootBuildImage
```

Then, run the container on Docker.

```bash
$ docker run --rm -p 8080:8080 book-service:0.0.1-SNAPSHOT
```

Finally, test the application works correctly.

```bash
$ http :8080/books
```

### Configure Cloud Native Buildpacks

You can configure the Cloud Native Buildpacks integration from the `build.gradle` file. For example, you can customize the name of the container image and the builder.

```groovy
tasks.named('bootBuildImage') {
  builder = 'paketobuildpacks/builder:base'
  imageName = "${project.name}"
}
```

Then, run the task again.

```bash
$ ./gradlew bootBuildImage
```

Now, the image name is simply `book-service`.

```bash
$ docker run --rm -p 8080:8080 book-service
```
