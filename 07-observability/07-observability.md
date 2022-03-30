# Observability

## Learning goals

* Exploring the Spring Boot Actuator endpoints
* Experimenting with observability capabilities in Grafana

## Overview

In this excercise, you will get familiar with the observability features in Spring Boot.

* Use Spring Boot Actuator to expose monitoring and management endpoints.
* Expose Prometheus metrics.
* Enable distributed tracing with OpenTelemetry.
* Use the Grafana observability stack.

## Details

### Containerize applications

As a pre-requisite, go into each project main folder (`begin/book-service`, `begin/suggestion-service`,
`begin/edge-service`) and run the following to package each application as a container image.

```bash
./gradlew bootBuildImage
```

### Run the system

Then, go to `begin` folder and run this command to start up all the containers in the system

```bash
$ docker-compose up -d
```

### Explore

Next, feel free to explore the several endpoints exposed by Spring Boot Actuator. For example, for Book Service, you can get the full list of
endpoints as follows.

```bash
$ http :8080/actuator
```

From Grafana (`http://localhost:3000`), log in as `user`/`password` and explore the following functionality:

* querying logs from Loki;
* accessing traces from Tempo;
* inspecting metrics from Prometheus;
* visualizing metrics from Grafana.
