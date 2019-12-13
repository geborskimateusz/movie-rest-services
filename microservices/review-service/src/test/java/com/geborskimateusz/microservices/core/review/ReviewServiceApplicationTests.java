package com.geborskimateusz.microservices.core.review;

import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.microservices.core.review.persistence.ReviewRepository;
import io.swagger.models.auth.In;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    public void contextLoads() {
    }

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
        int given = 0;

        webTestClient.get()
                .uri("/review?movieId=" + given)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8);
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

}
