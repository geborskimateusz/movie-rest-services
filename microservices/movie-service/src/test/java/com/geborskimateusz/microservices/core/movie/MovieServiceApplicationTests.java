package com.geborskimateusz.microservices.core.movie;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=RANDOM_PORT)
public class MovieServiceApplicationTests {

	@Autowired
	WebTestClient webTestClient;

	@Test
	public void contextLoads() {
	}

	@Test
	public void getMovie() {
		int given = 1;

		webTestClient.get()
				.uri("/movie/"+given)
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
				.expectBody()
				.jsonPath("$.movieId").isEqualTo(given)
				.jsonPath("$.genre").isNotEmpty()
				.jsonPath("$.title").isNotEmpty()
				.jsonPath("$.address").isNotEmpty();
	}

	@Test
	public void getMovieThrowsInvalidInputException() {
		String given = "no movie id";

		webTestClient.get()
				.uri("/movie/"+given)
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
				.uri("/movie/"+given)
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isNotFound();
	}

}
