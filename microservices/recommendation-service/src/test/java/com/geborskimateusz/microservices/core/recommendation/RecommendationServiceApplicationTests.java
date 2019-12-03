package com.geborskimateusz.microservices.core.recommendation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RecommendationServiceApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @Test
    public void contextLoads() {
    }

    @Test
    public void getRecommendations() {
        int given = 1;

        webTestClient.get()
                .uri("/recommendation?movieId=" + given)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0].movieId").isEqualTo(given);

    }

    @Test
    public void getRecommendationsThrowsInvalidInputException() {
		int given = 0;

		webTestClient.get()
				.uri("/recommendation?movieId=" + given)
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
				.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
				.expectBody()
				.jsonPath("$.message").isEqualTo("Invalid productId: " + given);


	}

}
