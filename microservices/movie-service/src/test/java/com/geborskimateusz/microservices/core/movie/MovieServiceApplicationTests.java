package com.geborskimateusz.microservices.core.movie;

import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.movie.MovieService;
import com.geborskimateusz.microservices.core.movie.persistence.MovieRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
public class MovieServiceApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MovieRepository movieRepository;


    @Test
    public void getMovie() {
        Integer given = 1;

        postAndVerify(given, HttpStatus.OK);

        assertTrue(movieRepository.findByMovieId(given).isPresent());

        getAndVerify(given, HttpStatus.OK)
                .jsonPath("$.movieId").isEqualTo(given)
                .jsonPath("$.genre").isNotEmpty()
                .jsonPath("$.title").isNotEmpty()
                .jsonPath("$.address").isNotEmpty();

    }

    @Test
    public void getMovieNotFound() {
        Integer given = 1;
        Integer requested = 2;

        postAndVerify(given, HttpStatus.OK);

        assertFalse(movieRepository.findByMovieId(requested).isPresent());

        getAndVerify(requested, HttpStatus.NOT_FOUND)
                .jsonPath("$.message").isEqualTo("No product found for productId: " + requested);
    }



    @Test
    public void getMovieThrowsInvalidInputExc() {
        Integer given = -1;

        postAndVerify(given, HttpStatus.OK);

        getAndVerify(given, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.message").isEqualTo("Invalid productId: "  + given);
    }


    @Test
    public void getMovieThrowsInvalidInputException() {
        String given = "no movie id";

        getAndVerify(given, HttpStatus.BAD_REQUEST);
    }

    //TODO
    @Test
    public void deleteMovie() {
        //deleteAndVerify
    }


    private WebTestClient.BodyContentSpec postAndVerify(Integer id, HttpStatus httpStatus) {
        Movie movie = Movie.builder()
                .movieId(id)
                .title("Title for movie " + id)
                .genre("Genre for movie " + id)
                .address("Address for movie " + id)
                .build();

        return webTestClient.post()
                .uri("/movie")
                .body(Mono.just(movie), Movie.class)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isEqualTo(httpStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec getAndVerify(Integer id, HttpStatus httpStatus) {
        return getAndVerify(id.toString(), httpStatus);
    }

        private WebTestClient.BodyContentSpec getAndVerify(String id, HttpStatus httpStatus) {
        return webTestClient.get()
                .uri("/movie/" + id)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isEqualTo(httpStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody();
    }

    @AfterEach
    void tearDown() {
        movieRepository.deleteAll();
    }

}
