# REST APIs and Data Persistence

## Learning goals

* Defining REST APIs with Spring WebFlux
* Persisting data with Spring Data R2DBC

## Overview

In this excercise, you will see how to implement a REST API and data persistence in the Book Service application.

* Define a 'Book' record with fields 'id' ('Long') and 'title' ('String').
* Make the 'id' field the primary key for the object in the database.
* Define a 'BookRepository' interface exposing CRUD methods.
* Use the repository to handle GET and POST requests in 'BookController'.

## Details

### Data entities and persistence

Spring Data JDBC and Spring Data R2DBC encourage using immutable objects, unlike Spring Data JPA that requires them to be mutable.
Since we're using Spring Data R2DBC, we can define a domain entity as a Java record.

```java
record Book(Long id, String title) {}
```

The object can be persisted without the need for further annotations or configurations. We use the @Id annotation when we would like the framework
to manage the id generation for us.

```java
record Book(Long id, String title) {}
```

### Data repositories

Spring Data is based on the concepts of domain-driven design (DDD). Each data entity is modeled as an aggregate, and repositories are used to interact
with the database. You can extend one of the interfaces provided by the framework to define a 'BookRepository' interface. At startup time, Spring Data
will generate an implementation for it automatically.

```java
interface BookRepository extends ReactiveCrudRepository<Book, Long> {}
```

### REST API

In Spring, you can design REST APIs either via '@RestController' classes or via 'RouterFunctions'. Let's use the first approach. Autowire the 'BookRepository'
bean in 'BookController' and implement methods for GET and POST requests for retrieving and creating books, respectively.

```java
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
```

The controller makes use of a few annotations:

* `@RestController` is a stereotype annotation marking a class as a Spring component and as a source of handlers
  for REST endpoints.
* `@RequestMapping` identifies the root path mapping URI for which the class provides handlers ("/books").
* `@GetMapping` maps HTTP GET requests onto the specific handler method.
* `@PostMapping` maps HTTP POST requests onto the specific handler method.
* `@ResponseStatus` returns a specific HTTP status if the request is successful.
* `@PathVariable` binds a method parameter to a URI template variable ({isbn}).
* `@RequestBody` binds a method parameter to the body of a web request.

Finally, run the application and test that it works correctly. Run the PostgreSQL container first:

```bash
$ docker-compose up -d
```

Then, add a book to the catalog.

```bash
$ http POST :8080/books title="The Hobbit"
```

Next, retrieve the full list of books in the catalog.

```bash
$ http :8080/books
```
