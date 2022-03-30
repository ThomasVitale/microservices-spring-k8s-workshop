package com.example.bookservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest
public class BookControllerTests {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    BookRepository bookRepository;

    @Test
    void getBookById() {
        var expectedBook = new Book(1L, "The Hobbit");
        when(bookRepository.findById(1L)).thenReturn(Mono.just(expectedBook));

        webTestClient
                .get()
                .uri("/books/1")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Book.class).isEqualTo(expectedBook);
    }

}
