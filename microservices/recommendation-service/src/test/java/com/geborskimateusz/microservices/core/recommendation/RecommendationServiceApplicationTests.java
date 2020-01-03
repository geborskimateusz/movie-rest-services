package com.geborskimateusz.microservices.core.recommendation;

import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.microservices.core.recommendation.persistence.RecommendationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static reactor.core.publisher.Mono.just;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@AutoConfigureWebTestClient(timeout = "10000")
public class RecommendationServiceApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    RecommendationRepository recommendationRepository;

    @Test
    public void getRecommendations() {
        String movieId = "1";


        postAndVerify(movieId, "2", HttpStatus.OK);
        postAndVerify(movieId, "3", HttpStatus.OK);
        postAndVerify(movieId, "4", HttpStatus.OK);


        assertEquals(Long.valueOf(3), recommendationRepository.count().block());

        getAndVerify(movieId, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].movieId").isEqualTo(movieId);
    }

    @Test
    public void getRecommendationsThrowsInvalidInputException() {
        String movieId = "0";

        getAndVerify(movieId, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.message").isEqualTo("Invalid movieId: " + movieId);
    }

    @Test
    public void getRecommendationsMissingParameter() {
        String given = "";

        getAndVerify(given, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createRecommendationThrowsDuplicateKeyException() {
        String movieId = "1";
        String recommendationId = "2";

        postAndVerify(movieId, recommendationId, HttpStatus.OK);

        assertEquals(Long.valueOf(1), recommendationRepository.count().block());


        postAndVerify(movieId, recommendationId, HttpStatus.INTERNAL_SERVER_ERROR)
        .jsonPath("$.message").isEqualTo("Non unique id for recommendation " + recommendationId);

        assertEquals(Long.valueOf(1), recommendationRepository.count().block());
    }



    @Test
    public void postRecommendations() {
        String movieId = "1";
        String recommendationId = "2";

        postAndVerify(movieId, recommendationId, HttpStatus.OK);

        assertEquals(Long.valueOf(1), recommendationRepository.count().block());

        getAndVerify(movieId, HttpStatus.OK)
                .jsonPath("$[0].movieId").isEqualTo(movieId)
                .jsonPath("$[0].recommendationId").isEqualTo(recommendationId);

    }

    @Test
    public void deleteRecommendations() {
        String movieId = "1";

        postAndVerify(movieId, "2", HttpStatus.OK);
        postAndVerify(movieId, "3", HttpStatus.OK);
        postAndVerify(movieId, "4", HttpStatus.OK);

        assertEquals(3, recommendationRepository.findByMovieId(Integer.parseInt(movieId)).collectList().block().size());

        deleteAndVerify(movieId, HttpStatus.OK);

        assertEquals(0, recommendationRepository.findByMovieId(Integer.parseInt(movieId)).collectList().block().size());
    }

    @Test
    public void deleteRecommendationsThrowsInvalidInputException() {
        String invalidMovieId = "0";
        String validMovieId = "1";

        postAndVerify(validMovieId, "2", HttpStatus.OK);
        postAndVerify(validMovieId, "3", HttpStatus.OK);
        postAndVerify(validMovieId, "4", HttpStatus.OK);

        assertEquals(3, recommendationRepository.findByMovieId(Integer.parseInt(validMovieId)).collectList().block().size());

        deleteAndVerify(invalidMovieId, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.message").isEqualTo("Invalid movieId: " + invalidMovieId);

        assertEquals(3, recommendationRepository.findByMovieId(Integer.parseInt(validMovieId)).collectList().block().size());
    }


    private WebTestClient.BodyContentSpec getAndVerify(String movieId, HttpStatus httpStatus) {
        return webTestClient.get()
                .uri("/recommendation?movieId=" + movieId)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isEqualTo(httpStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody();
    }


    private WebTestClient.BodyContentSpec postAndVerify(String movieId, String recommendationId, HttpStatus httpStatus) {

        Recommendation recommendation = Recommendation.builder().movieId(Integer.parseInt(movieId)).recommendationId(Integer.parseInt(recommendationId)).author("Author 3").rate(3).content("Content 3").build();

        return webTestClient.post()
                .uri("/recommendation")
                .body(just(recommendation), Recommendation.class)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isEqualTo(httpStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody();

    }

    private WebTestClient.BodyContentSpec deleteAndVerify(String movieId, HttpStatus httpStatus) {
        return webTestClient.delete()
                .uri("/recommendation?movieId="+movieId)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isEqualTo(httpStatus)
                .expectBody();
    }

    @AfterEach
    public void cleanUp()  {
        recommendationRepository.deleteAll().block();
    }

}
