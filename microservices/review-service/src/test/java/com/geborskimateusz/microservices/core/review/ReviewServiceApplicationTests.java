package com.geborskimateusz.microservices.core.review;

import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.microservices.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static reactor.core.publisher.Mono.just;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:review-db"})
public class ReviewServiceApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewRepository reviewRepository;

    @Test
    public void getReviews() {
        String movieId = "1";

        postAndVerify(movieId, "2", HttpStatus.OK);
        postAndVerify(movieId, "3", HttpStatus.OK);
        postAndVerify(movieId, "4", HttpStatus.OK);

        assertEquals(3, reviewRepository.count());
        getAndVerify(movieId, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(3);
    }

    @Test
    public void getReviewsThrowsInvalidInputException() {
        String movieId = "-1";

        getAndVerify(movieId, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.message").isEqualTo("Invalid productId: " + movieId);
    }

    @Test
    public void getReviewsMissingParameter() {
        String movieId = "";

        getAndVerify(movieId, HttpStatus.BAD_REQUEST)
                .jsonPath("$.message").isEqualTo("Type mismatch.");
    }

    @Test
    public void createReview() {
        String movieId = "1";
        String reviewId = "2";

        postAndVerify(movieId, reviewId, HttpStatus.OK);

        assertEquals(1, reviewRepository.count());

        getAndVerify(movieId, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].reviewId").isEqualTo(reviewId)
                .jsonPath("$[0].movieId").isEqualTo(movieId);
    }

    @Test
    public void createReviewDataIntegrityViolationException() {
        String movieId = "1";
        String reviewId = "2";

        postAndVerify(movieId, reviewId, HttpStatus.OK);

        assertEquals(1, reviewRepository.count());

        getAndVerify(movieId, HttpStatus.OK)
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].reviewId").isEqualTo(reviewId)
                .jsonPath("$[0].movieId").isEqualTo(movieId);

        postAndVerify(movieId, reviewId, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.message").isEqualTo("Duplicate key, Movie Id: " + movieId + ", Review Id:" + reviewId);

        assertEquals(1, reviewRepository.count());
    }

    @Test
    public void deleteReviews() {
        String movieId = "1";

        postAndVerify(movieId, "2", HttpStatus.OK);
        postAndVerify(movieId, "3", HttpStatus.OK);

        assertEquals(2, reviewRepository.count());

        deleteAndVerify(movieId, HttpStatus.OK);

        assertEquals(0, reviewRepository.count());
    }

    public WebTestClient.BodyContentSpec deleteAndVerify(String movieId, HttpStatus status) {
        return webTestClient.delete()
                .uri("/review?movieId="+movieId)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isEqualTo(status)
                .expectBody();
    }

    public WebTestClient.BodyContentSpec postAndVerify(String movieId, String reviewId, HttpStatus httpStatus) {
        Review review = Review.builder()
                .movieId(Integer.parseInt(movieId))
                .reviewId(Integer.parseInt(reviewId))
                .author("Author 1")
                .subject("Subject 1")
                .content("Content 1")
                .serviceAddress("Fake Address")
                .build();

        return webTestClient.post()
                .uri("/review")
                .body(just(review), Review.class)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isEqualTo(httpStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec getAndVerify(String movieId, HttpStatus status) {
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
