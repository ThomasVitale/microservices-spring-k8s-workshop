# API Gateway

## Learning goals

* Using Spring Cloud Gateway
* Working with routes, predicates, and filters

## Overview

In this excercise, you will see how to use Spring Cloud Gateway to implement an `Edge Service` consisting of an API gateway and cross-cutting concerns.

* Define two routes to Book Service and Suggestion Service.
* Configure the predicates to match against the request path.
* Use filters to add request and response headers.
* Use filters to make the system more resilient.

## Details

### Defining routes

In the Edge Service project (`begin/edge-service`), start by defining two routes in the `application.yml` file.
They will route traffic to Book Service and Suggestion Service based on the request path.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: book-route
          uri: ${BOOK_SERVICE_URI:http://localhost:8080}
          predicates:
            - Path=/books/**
        - id: suggestion-route
          uri: ${SUGGESTION_SERVICE_URI:http://localhost:8181}
          predicates:
            - Path=/suggestions/**
```

### Using filters

Spring Cloud Gateway provides many built-in filters to manipulate requests and responses. For example, you can add headers.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: book-route
          uri: ${BOOK_SERVICE_URI:http://localhost:8080}
          predicates:
            - Path=/books/**
          filters:
            - AddResponseHeader=X-Genre,fantasy
        - id: suggestion-route
          uri: ${SUGGESTION_SERVICE_URI:http://localhost:8181}
          predicates:
            - Path=/suggestions/**
          filters:
            - AddRequestHeader=X-Tenant,acme
```

### Resilience

Being at the edge, this application is a great place to implement cross-cutting concerns like resilience and security. Spring Cloud Gateway supports
retries, rate limiters, circuit breakers, and OAuth2/OpenID Connect.

As an example, let's configure a default filter to retry GET requests that fail.

```yaml
spring:
  cloud:
    gateway:
      routes:
       ...
      default-filters:
        - name: Retry
          args:
            retries: 5
            methods: GET
            backoff:
              firstBackoff: 50ms
              maxBackoff: 500ms
              factor: 2
```

### Run an API gateway

First, run Book Service and Suggestion Service (`./gradlew bootRun`). Then, run Edge Service as well (`./gradlew bootRun`). This time, you can access both APIs
via the gateway.

```bash
$ http :9000/books
$ http :9000/suggestions
```

### Explore

Check the Spring Cloud Gateway [documentation](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/) and try using some of the filters provided.

