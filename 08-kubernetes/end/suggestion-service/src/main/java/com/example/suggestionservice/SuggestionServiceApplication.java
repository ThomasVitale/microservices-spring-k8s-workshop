package com.example.suggestionservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SuggestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuggestionServiceApplication.class, args);
    }

}

@ConfigurationProperties(prefix = "demo")
record DemoProperties(URI bookServiceUri) {}

record Suggestion(String title) {}

@RestController
class SuggestionController {

    private static final Logger log = LoggerFactory.getLogger(SuggestionController.class);
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
                .onErrorResume(Exception.class, exception -> Flux.empty())
                .doOnNext(suggestion -> log.info("Computing book suggestions"));
    }

}
