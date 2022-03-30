package com.example.bookservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class BookServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookServiceApplication.class, args);
    }

}

@RestController
@RequestMapping("books")
class BookController {

    @GetMapping
    Flux<String> getAllBooks() {
        return Flux.just("The Lord of the Rings");
    }

}
