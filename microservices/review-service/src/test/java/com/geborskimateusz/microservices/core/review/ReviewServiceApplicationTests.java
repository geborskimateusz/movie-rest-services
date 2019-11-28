package com.geborskimateusz.microservices.core.review;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReviewServiceApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Test
    public void contextLoads() {
    }

    @Test
    public void getReviews() {
        int given = 1;

        webTestClient.get()
                .uri("/review?movieId=" + given)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
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

}
