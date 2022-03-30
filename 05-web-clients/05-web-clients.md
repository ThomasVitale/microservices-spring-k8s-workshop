# Web Clients

## Learning goals

* Using the Spring WebClient 
* Understanding reactive streams and reslience

## Overview

In this excercise, you will see how to use `WebClient` for service-to-service interactions between Suggestion Service and Book Service.

* Define a `Suggestion` record with a `title` field of type `String`.
* Create a `SuggestionController` to handle GET requests to the `/suggestions` endpoint.
* Build a `WebClient` object in the controller constructor usign the base URL passed via a custom property.
* Use the `WebClient` to call Book Service, fetch 2 books, and return their titles to the user.

## Details

### Domain model

For this feature, we'll need a domain object to model book suggestions. Let's define it as a record.

```java
record Suggestion(String title) {}
```

### Custom configuration properties

Then, we want to externalized the configuration of the Book Service URL. We can do that via a `@ConfigurationProperties` bean.

Define the bean as follows. It will automatically parse the value for a `demo.book-service-uri` property.

```java
@ConfigurationProperties(prefix = "demo")
record DemoProperties(URI bookServiceUri) {}
```

Then, instruct Spring Boot to scan the project for `@ConfigurationProperties` beans.

```java
@SpringBootApplication
@ConfigurationPropertiesScan
public class SuggestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuggestionServiceApplication.class, args);
    }

}
```

Finally, define a default value in `application.yml` for the brand new `demo.book-service-uri` property.

```yaml
demo:
  book-service-uri: http://localhost:8080
```

### WebClient

At this point, we need to do a few things:

* Create a new `SuggestionController` class and mark it as `@RestController`.
* Initialize a `WebClient` object in the constructor, using `DemoProperties` to fetch the base URL and `WebClient.Builder`.
* Define a method to handle GET requests to the `/suggestions` endpoint.
* Implement the method by using the `WebClient` to call the Book Service, get 2 books back, and return them as a `Flux<Suggestion>`.
* Feel free to make the interaction more resilient using timeouts, retries, and fallbacks.

```java
@RestController
class SuggestionController {

    private final WebClient webClient;

    SuggestionController(DemoProperties properties, WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl(properties.bookServiceUri().toString())
                .build();
    }

    @GetMapping("suggestions")
    Flux<Suggestion> getSuggestion() {
        return webClient
                .get()
                .uri("/books")
                .retrieve()
                .bodyToFlux(Suggestion.class)
                .take(2)
                .timeout(Duration.ofSeconds(2), Flux.empty())
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
                .onErrorResume(Exception.class, exception -> Flux.empty());
    }

}
```

Finally, run Book Service and create some books (at least three). Then, run Suggestion Service and run the following to verify that it works.

```bash
http :8181/suggestions
```

Then, try shutting down Book Service and then call Suggestion Service again. Notice how the application will not fail, but it will return the configured
fallback result: an empty result.
