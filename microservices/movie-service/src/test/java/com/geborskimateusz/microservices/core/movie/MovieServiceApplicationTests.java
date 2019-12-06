package com.geborskimateusz.microservices.core.movie;

import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.movie.MovieService;
import com.geborskimateusz.microservices.core.movie.persistence.MovieEntity;
import com.geborskimateusz.microservices.core.movie.persistence.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
public class MovieServiceApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MovieService movieService;

    @Test
    public void getMovie() {
        Integer given = 1;

        postAndVerify(given, HttpStatus.OK);

        Movie movie = movieService.getMovie(given);
        assertNotNull(movie);

//        webTestClient.get()
//                .uri("/movie/" + given)
//                .accept(MediaType.APPLICATION_JSON_UTF8)
//                .exchange()
//                .expectStatus().isOk()
//                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
//                .expectBody()
//                .jsonPath("$.movieId").isEqualTo(given)
//                .jsonPath("$.genre").isNotEmpty()
//                .jsonPath("$.title").isNotEmpty()
//                .jsonPath("$.address").isNotEmpty();
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

    @Test
    public void getMovieThrowsInvalidInputException() {
        String given = "no movie id";

        webTestClient.get()
                .uri("/movie/" + given)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void getMovieThrowsNotFoundException() {
        int given = 13;

        webTestClient.get()
                .uri("/movie/" + given)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isNotFound();
    }

}
