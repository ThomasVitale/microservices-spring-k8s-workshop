package com.example.bookservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class BookServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookServiceApplication.class, args);
    }

}

record Book(@Id Long id, String title) {}

interface BookRepository extends ReactiveCrudRepository<Book, Long> {}

@RestController
@RequestMapping("books")
class BookController {

    private final BookRepository bookRepository;

    BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping
    Flux<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("{id}")
    Mono<Book> getBookById(@PathVariable Long id) {
        return bookRepository.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Mono<Book> createBook(@RequestBody Book book) {
        return bookRepository.save(book);
    }

}
