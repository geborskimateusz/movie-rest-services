package com.geborskimateusz.microservices.core.movie;

import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.movie.MovieService;
import com.geborskimateusz.api.event.Event;
import com.geborskimateusz.microservices.core.movie.persistence.MovieEntity;
import com.geborskimateusz.microservices.core.movie.persistence.MovieRepository;
import io.swagger.models.auth.In;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
public class MovieServiceApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    private Sink channels;

    private AbstractMessageChannel input = null;

    @BeforeEach
    void setUp() {
        input = (AbstractMessageChannel) channels.input();
        movieRepository.deleteAll().block();
    }

    @Test
    public void getMovie() {
        Integer given = 1;

        assertNull(movieRepository.findByMovieId(given).block());
        assertEquals(0,(long )movieRepository.count().block());

        sendCreateMovie(given);

        assertNotNull(movieRepository.findByMovieId(given).block());
        assertEquals(1,(long )movieRepository.count().block());

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

        sendCreateMovie(given);

        assertFalse(movieRepository.findByMovieId(requested).blockOptional().isPresent());

        getAndVerify(requested, HttpStatus.NOT_FOUND)
                .jsonPath("$.message").isEqualTo("No movie found for movieId: " + requested);
    }


    @Test
    public void getMovieThrowsInvalidInputExc() {
        Integer given = -1;

        sendCreateMovie(given);

        getAndVerify(given, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.message").isEqualTo("Invalid movieId: " + given);
    }


    @Test
    public void getMovieThrowsInvalidInputException() {
        String given = "no movie id";

        getAndVerify(given, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void deleteMovie() {
        Integer given = 1;

        sendCreateMovie(given);

        assertTrue(movieRepository.findByMovieId(given).blockOptional().isPresent());

        sendDeleteMovie(given);

        assertNull(movieRepository.findByMovieId(given).block());
        assertEquals(0,(long )movieRepository.count().block());
    }

    private void sendDeleteMovie(Integer given) {
        Event<Integer, Movie> event = new Event<>(Event.Type.DELETE,given,null);
        input.send(new GenericMessage<>(event));
    }


    private WebTestClient.BodyContentSpec postAndVerify(Integer id, HttpStatus httpStatus) {
        Movie movie = getMovie(id);

        return webTestClient.post()
                .uri("/movie")
                .body(Mono.just(movie), Movie.class)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isEqualTo(httpStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody();
    }

    private Movie getMovie(Integer id) {
        return Movie.builder()
                .movieId(id)
                .title("Title for movie " + id)
                .genre("Genre for movie " + id)
                .address("Address for movie " + id)
                .build();
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

    private WebTestClient.BodyContentSpec deleteAndVerify(Integer id, HttpStatus httpStatus) {
        return webTestClient.delete()
                .uri("/movie/" + id)
                .exchange()
                .expectStatus().isEqualTo(httpStatus)
                .expectBody();
    }

    private void sendCreateMovie(Integer given) {
        Movie movie = getMovie(given);
        Event<Integer, Movie> event = new Event<>(Event.Type.CREATE, given, movie);
        input.send(new GenericMessage<>(event));
    }


    @AfterEach
    void tearDown() {
        movieRepository.deleteAll().block();
    }

}
