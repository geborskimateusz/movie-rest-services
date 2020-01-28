package com.geborskimateusz.microservices.core.recommendation;

import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.event.Event;
import com.geborskimateusz.microservices.core.recommendation.persistence.RecommendationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Java6Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
public class RecommendationServiceApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    RecommendationRepository recommendationRepository;

    @Autowired
    Sink sink;

    AbstractMessageChannel input = null;

    @BeforeEach
    void setUp() {
        input = (AbstractMessageChannel) sink.input();
        recommendationRepository.deleteAll().block();
    }


    @Test
    public void getRecommendations() {


        int movieId = 1;
        sendCreateRecommendationEvent(movieId, 2);
        sendCreateRecommendationEvent(movieId, 3);
        sendCreateRecommendationEvent(movieId, 4);


        assertEquals(3, (long) recommendationRepository.count().block());

        getAndVerify(movieId, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].movieId").isEqualTo(movieId);
    }

    @Test
    public void getRecommendationsThrowsInvalidInputException() {
        Integer movieId = 0;

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
        Integer movieId = 1;
        Integer recommendationId = 2;

        sendCreateRecommendationEvent(movieId, recommendationId);

        assertEquals(Long.valueOf(1), recommendationRepository.count().block());


        try {
            sendCreateRecommendationEvent(movieId, recommendationId);
            fail("Expected a MessagingException here!");
        } catch (MessagingException me) {
            if (me.getCause() instanceof DuplicateKeyException) {
                DuplicateKeyException iie = (DuplicateKeyException) me.getCause();
                assertEquals("Non unique id for recommendation 2", iie.getMessage());
            } else {
                fail("Expected a InvalidInputException as the root cause!");
            }
        }

        assertEquals(1, (long) recommendationRepository.count().block());
    }


    @Test
    public void postRecommendations() {
        Integer movieId = 1;
        Integer recommendationId = 2;

        sendCreateRecommendationEvent(movieId, recommendationId);

        assertEquals(Long.valueOf(1), recommendationRepository.count().block());

        getAndVerify(movieId, HttpStatus.OK)
                .jsonPath("$[0].movieId").isEqualTo(movieId)
                .jsonPath("$[0].recommendationId").isEqualTo(recommendationId);

    }

    @Test
    public void deleteRecommendations() {
        Integer movieId = 1;

        sendCreateRecommendationEvent(movieId, 2);
        sendCreateRecommendationEvent(movieId, 3);
        sendCreateRecommendationEvent(movieId, 4);

        assertEquals(3, recommendationRepository.findByMovieId(movieId).collectList().block().size());

        deleteAndVerify(movieId);

        assertEquals(0, recommendationRepository.findByMovieId(movieId).collectList().block().size());
    }


    private WebTestClient.BodyContentSpec getAndVerify(Integer movieId, HttpStatus httpStatus) {
        return getAndVerify(movieId.toString(), httpStatus);
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

    private void sendCreateRecommendationEvent(int movieId, int recommendationId) {
        Recommendation recommendation = Recommendation.builder().movieId(movieId).recommendationId(recommendationId).author("Author 3").rate(3).content("Content 3").build();
        Event event = new Event(Event.Type.CREATE, movieId,recommendation);
        input.send(new GenericMessage<>(event));
    }

    private void deleteAndVerify(int movieId) {
        Event event = new Event(Event.Type.DELETE,movieId,null);
        input.send(new GenericMessage<>(event));
    }

    @AfterEach
    public void cleanUp() {
        recommendationRepository.deleteAll().block();
    }

}
