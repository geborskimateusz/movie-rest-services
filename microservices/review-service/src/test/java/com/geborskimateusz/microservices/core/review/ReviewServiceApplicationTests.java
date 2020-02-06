package com.geborskimateusz.microservices.core.review;

import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.api.event.Event;
import com.geborskimateusz.microservices.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:review-db", "eureka.client.enabled=false"})
public class ReviewServiceApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    Sink channels;

    private AbstractMessageChannel input = null;

    @BeforeEach
    void setUp() {
        input = (AbstractMessageChannel) channels.input();
    }

    @Test
    public void getReviews() {
        Integer movieId = 1;

        sendCreateReview(1, movieId);
        sendCreateReview(2, movieId);

        int testId = 3;
        sendCreateReview(testId, movieId);

        assertEquals(3, reviewRepository.count());
        getAndVerify(movieId, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$.[2].reviewId").isEqualTo(testId)
                .jsonPath("$.[2].movieId").isEqualTo(movieId);
    }

    @Test
    public void getReviewsThrowsInvalidInputException() {
        Integer movieId = -1;

        getAndVerify(movieId, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.message").isEqualTo("Invalid productId: " + movieId);
    }

    @Test
    public void getReviewsMissingParameter() {
        Integer movieId = null;

        getAndVerify(movieId, HttpStatus.BAD_REQUEST)
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void createReview() {
        Integer movieId = 1;
        Integer reviewId = 2;

        sendCreateReview(reviewId, movieId);

        assertEquals(1, reviewRepository.count());

        getAndVerify(movieId, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].reviewId").isEqualTo(reviewId)
                .jsonPath("$[0].movieId").isEqualTo(movieId);
    }

    @Test
    public void createReviewInvalidInputException() {
        Integer movieId = 1;
        Integer reviewId = 2;

        assertThrows(MessagingException.class, () -> {
            sendCreateReview(reviewId, movieId);

            assertEquals(1, reviewRepository.count());

            getAndVerify(movieId, HttpStatus.OK)
                    .jsonPath("$.length()").isEqualTo(1)
                    .jsonPath("$[0].reviewId").isEqualTo(reviewId)
                    .jsonPath("$[0].movieId").isEqualTo(movieId);

            sendCreateReview(reviewId, movieId);

        });
    }

    @Test
    public void deleteReviews() {
        Integer movieId = 1;

        sendCreateReview(2, movieId);
        sendCreateReview(3, movieId);

        assertEquals(2, reviewRepository.count());

        deleteAndVerify(movieId, HttpStatus.OK);

        assertEquals(0, reviewRepository.count());
    }

    public WebTestClient.BodyContentSpec deleteAndVerify(Integer movieId, HttpStatus status) {
        return webTestClient.delete()
                .uri("/review?movieId=" + movieId)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isEqualTo(status)
                .expectBody();
    }

//    public WebTestClient.BodyContentSpec postAndVerify(String movieId, String reviewId, HttpStatus httpStatus) {
//        Review review = Review.builder()
//                .movieId(Integer.parseInt(movieId))
//                .reviewId(Integer.parseInt(reviewId))
//                .author("Author 1")
//                .subject("Subject 1")
//                .content("Content 1")
//                .serviceAddress("Fake Address")
//                .build();
//
//        return webTestClient.post()
//                .uri("/review")
//                .body(just(review), Review.class)
//                .accept(MediaType.APPLICATION_JSON_UTF8)
//                .exchange()
//                .expectStatus().isEqualTo(httpStatus)
//                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
//                .expectBody();
//    }

    private void sendCreateReview(Integer reviewId, Integer movieId) {
        Review review = Review.builder()
                .movieId(movieId)
                .reviewId(reviewId)
                .author("Author 1")
                .subject("Subject 1")
                .content("Content 1")
                .serviceAddress("Fake Address")
                .build();

        Event<Integer, Review> event = new Event(Event.Type.CREATE, movieId, review);
        input.send(new GenericMessage<>(event));
    }

    private WebTestClient.BodyContentSpec getAndVerify(Integer movieId, HttpStatus status) {
        return webTestClient.get()
                .uri("/review?movieId=" + movieId)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isEqualTo(status)
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody();
    }

    @AfterEach
    public void cleanUp() {
        reviewRepository.deleteAll();
    }
}
