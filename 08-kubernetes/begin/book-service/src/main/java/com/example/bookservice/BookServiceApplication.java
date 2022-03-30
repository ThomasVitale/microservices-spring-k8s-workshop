package com.example.bookservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(BookController.class);
    private final BookRepository bookRepository;

    BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping
    Flux<Book> getAllBooks() {
        log.info("Fetching all books");
        return bookRepository.findAll();
    }

    @GetMapping("{id}")
    Mono<Book> getBookById(@PathVariable Long id) {
        log.info("Fetching book with id: {}", id);
        return bookRepository.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Mono<Book> createBook(@RequestBody Book book) {
        log.info("Creating book with title: {}", book.title());
        return bookRepository.save(book);
    }

}
