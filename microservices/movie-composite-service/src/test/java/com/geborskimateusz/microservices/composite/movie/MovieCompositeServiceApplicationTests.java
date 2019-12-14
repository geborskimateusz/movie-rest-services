package com.geborskimateusz.microservices.composite.movie;

import com.geborskimateusz.api.composite.movie.MovieAggregate;
import com.geborskimateusz.api.composite.movie.RecommendationSummary;
import com.geborskimateusz.api.composite.movie.ReviewSummary;
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
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static reactor.core.publisher.Mono.just;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class MovieCompositeServiceApplicationTests {

    public static final String FAKE_ADDRESS = "Fake address";
    public static final String FAKE_GENRE = "Fake genre";
    public static final String FAKE_TITLE = "Fake title";
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    MovieCompositeIntegration movieCompositeIntegration;

    @MockBean
    ServiceUtil serviceUtil;

    @Test
    void createMovie() {
        int movieId = 1;

        MovieAggregate movieAggregate = MovieAggregate.builder()
                .movieId(movieId)
                .genre(FAKE_GENRE)
                .title(FAKE_TITLE)
                .recommendations(getRecommendationSummaries())
                .reviews(getReviewSummaries())
                .serviceAddresses(null)
                .build();

        postAndVerify(movieAggregate);
    }

    @Test
    void getMovieById() {
        int given = 1;

        Movie movie = Movie.builder().movieId(given).address(FAKE_ADDRESS).genre(FAKE_GENRE).title(FAKE_TITLE).build();

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

        getAndVerifyMovie(given)
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

    private WebTestClient.BodyContentSpec getAndVerifyMovie(int given) {
        return webTestClient.get()
                .uri("/movie-composite/" + given)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody();
    }



    private List<ReviewSummary> getReviewSummaries() {
        return Collections.singletonList(ReviewSummary.builder().reviewId(1).subject("s").author("a").content("c").build());
    }

    private List<RecommendationSummary> getRecommendationSummaries() {
        return Collections.singletonList(RecommendationSummary.builder().recommendationId(1).author("a").content("c").rate(1).build());
    }

    private void postAndVerify(MovieAggregate movieAggregate) {
        webTestClient.post()
                .uri("/movie-composite")
                .body(just(movieAggregate), MovieAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK);
    }
}
