# REST APIs and Data Persistence

## Learning goals

* Understanding the slice tests in Spring Boot
* Implementing integration tests with Spring and Testcontainers

## Overview

In this excercise, you will see how to test an application.

* Define a `BookControllerTests` class to the test the web slice of the application.
* Update the `BookServiceApplicationTests` class to the test the full application using Testcontainers.

## Details

### Slice tests

Spring Boot provides annotation to define tests for many different slices. When it comes to reactive web controllers, you can use the `WebFluxTest` annotation.
For sending HTTP requests and verify the result, you can use the `WebTestClient` utility provided by the framework. Any bean that is not in the web slice
(for example, `BookRepository`) need to be mocked.

```java
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
```

When you're done, run the tests as follows:

```bash
./gradlew test --tests BookControllerTests
```

### Integration tests

Full integration tests are possible via the `@SpringBootTest` annotation. Testcontainer helps implementing integration tests with environment parity.
In Book Service, you can use it to have a real PostgreSQL database running in a lightweight container as part of the test process.
The `@TestPropertySource` annotation can be used to overwrite the PostgreSQL URL to point to the instance provided by Testcontainer under the hood.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.r2dbc.url=r2dbc:tc:postgresql:///catalog?TC_IMAGE_TAG=14.1")
class BookServiceApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void addBookToCatalog() {
        var bookToCreate = new Book(null, "The Hobbit");

        webTestClient
                .post()
                .uri("/books")
                .bodyValue(bookToCreate)
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook.id()).isNotNull();
                    assertThat(actualBook.title()).isEqualTo(bookToCreate.title());
                });
    }

}
```

When you're done, run the tests as follows:

```bash
./gradlew test --tests BookServiceApplicationTests
```
