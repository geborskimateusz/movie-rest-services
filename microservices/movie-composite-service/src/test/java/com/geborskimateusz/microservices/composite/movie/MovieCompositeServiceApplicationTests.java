package com.geborskimateusz.microservices.composite.movie;

import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.microservices.composite.movie.services.MovieCompositeIntegration;
import com.geborskimateusz.util.exceptions.InvalidInputException;
import com.geborskimateusz.util.exceptions.NotFoundException;
import com.geborskimateusz.util.http.ServiceUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Arrays;
import java.util.List;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class MovieCompositeServiceApplicationTests {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    MovieCompositeIntegration movieCompositeIntegration;

    @MockBean
    ServiceUtil serviceUtil;

    @Test
    public void contextLoads() {
    }

    @Test
    void getMovieById() {
        int given = 1;

        Movie movie = Movie.builder().movieId(given).address("Fake address").genre("Fake genre").title("Fake title").build();

        Mockito.when(serviceUtil.getServiceAddress()).thenReturn("Fake service address");

        List<Recommendation> recommendations = Arrays.asList(
                Recommendation.builder().movieId(movie.getMovieId()).recommendationId(1).author("Author 1").rate(1).content("Content 1").serviceAddress(serviceUtil.getServiceAddress()).build(),
                Recommendation.builder().movieId(movie.getMovieId()).recommendationId(2).author("Author 2").rate(2).content("Content 2").serviceAddress(serviceUtil.getServiceAddress()).build(),
                Recommendation.builder().movieId(movie.getMovieId()).recommendationId(3).author("Author 3").rate(3).content("Content 3").serviceAddress(serviceUtil.getServiceAddress()).build()
        );
        List<Review> reviews = Arrays.asList(
                Review.builder().movieId(movie.getMovieId()).reviewId(1).author("Author 1").subject("Subject 1").content("Content 1").serviceAddress(serviceUtil.getServiceAddress()).build(),
                Review.builder().movieId(movie.getMovieId()).reviewId(2).author("Author 2").subject("Subject 2").content("Content 2").serviceAddress(serviceUtil.getServiceAddress()).build(),
                Review.builder().movieId(movie.getMovieId()).reviewId(3).author("Author 2").subject("Subject 3").content("Content 3").serviceAddress(serviceUtil.getServiceAddress()).build()
        );

        Mockito.when(movieCompositeIntegration.getMovie(given)).thenReturn(movie);
        Mockito.when(movieCompositeIntegration.getRecommendations(movie.getMovieId())).thenReturn(recommendations);
        Mockito.when(movieCompositeIntegration.getReviews(movie.getMovieId())).thenReturn(reviews);

        webTestClient.get()
                .uri("/movie-composite/" + given)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.movieId").isEqualTo(given)
                .jsonPath("$.recommendations.length()").isEqualTo(3)
                .jsonPath("$.reviews.length()").isEqualTo(3);
    }

    @Test
    void getMovieByIdThrowsNotFoundException() {
        int given = 1;

        Mockito.when(movieCompositeIntegration.getMovie(given)).thenThrow(NotFoundException.class);

        webTestClient.get()
                .uri("/movie-composite/" + given)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.path").isEqualTo("/movie-composite/" + given);
	}

    @Test
    void getMovieByIdThrowsInvalidInputException() {
		int given = 1;

		Mockito.when(movieCompositeIntegration.getMovie(given)).thenThrow(InvalidInputException.class);

		webTestClient.get()
				.uri("/movie-composite/" + given)
				.accept(MediaType.APPLICATION_JSON_UTF8)
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/movie-composite/" + given);
    }


}
