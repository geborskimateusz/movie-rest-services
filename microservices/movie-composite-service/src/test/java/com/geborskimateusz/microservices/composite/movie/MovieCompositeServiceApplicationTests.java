package com.geborskimateusz.microservices.composite.movie;

import com.geborskimateusz.api.composite.movie.MovieAggregate;
import com.geborskimateusz.api.composite.movie.RecommendationSummary;
import com.geborskimateusz.api.composite.movie.ReviewSummary;
import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.microservices.composite.movie.services.BaseMovieCompositeService;
import com.geborskimateusz.microservices.composite.movie.services.MovieCompositeIntegration;
import com.geborskimateusz.util.exceptions.InvalidInputException;
import com.geborskimateusz.util.exceptions.NotFoundException;
import com.geborskimateusz.util.http.ServiceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

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

        Movie movie = getMovies(given);

        Mockito.when(serviceUtil.getServiceAddress()).thenReturn("Fake service address");

        List<Recommendation> recommendations = getRecommendations(movie);
        List<Review> reviews = getReviews(movie);

        Mockito.when(movieCompositeIntegration.getMovie(given)).thenReturn(Mono.just(movie));
        Mockito.when(movieCompositeIntegration.getRecommendations(movie.getMovieId())).thenReturn(Flux.fromIterable(recommendations));
        Mockito.when(movieCompositeIntegration.getReviews(movie.getMovieId())).thenReturn(Flux.fromIterable(reviews));

        getAndVerifyMovie(given, HttpStatus.OK)
                .jsonPath("$.movieId").isEqualTo(given)
                .jsonPath("$.recommendations.length()").isEqualTo(recommendations.size())
                .jsonPath("$.reviews.length()").isEqualTo(reviews.size());
    }

    @Test
    void getMovieByIdThrowsNotFoundException() {
        int given = 1;

        Mockito.when(movieCompositeIntegration.getMovie(given)).thenThrow(NotFoundException.class);

        getAndVerifyMovie(given, HttpStatus.NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/movie-composite/" + given);
	}

    @Test
    void getMovieByIdThrowsInvalidInputException() {
		int given = 0;

		Mockito.when(movieCompositeIntegration.getMovie(given)).thenThrow(InvalidInputException.class);

		getAndVerifyMovie(given, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/movie-composite/" + given);
    }

    @Test
    void deleteCompositeMovie() {
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

        int given = movieId;

        deleteAndVerify(given, HttpStatus.OK);

        verify(movieCompositeIntegration, times(1)).deleteMovie(given);
        verify(movieCompositeIntegration, times(movieAggregate.getReviews().size())).deleteReviews(given);
        verify(movieCompositeIntegration, times(movieAggregate.getRecommendations().size())).deleteRecommendations(given);
    }

    private WebTestClient.BodyContentSpec getAndVerifyMovie(int movieId, HttpStatus status) {
        return webTestClient.get()
                .uri("/movie-composite/" + movieId)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isEqualTo(status)
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec postAndVerify(MovieAggregate movieAggregate) {
        return webTestClient.post()
                .uri("/movie-composite")
                .body(just(movieAggregate), MovieAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.OK)
                .expectBody();
    }

    private void deleteAndVerify(int movieId, HttpStatus httpStatus) {
         webTestClient.delete()
                .uri("/movie-composite/" + movieId)
                .exchange()
                .expectStatus().isEqualTo(httpStatus);
    }

    private List<ReviewSummary> getReviewSummaries() {
        return Collections.singletonList(ReviewSummary.builder().reviewId(1).subject("s").author("a").content("c").build());
    }

    private List<RecommendationSummary> getRecommendationSummaries() {
        return Collections.singletonList(RecommendationSummary.builder().recommendationId(1).author("a").content("c").rate(1).build());
    }

    private Movie getMovies(int given) {
        return Movie.builder().movieId(given).address(FAKE_ADDRESS).genre(FAKE_GENRE).title(FAKE_TITLE).build();
    }

    private List<Review> getReviews(Movie movie) {
        return Arrays.asList(
                Review.builder().movieId(movie.getMovieId()).reviewId(1).author("Author 1").subject("Subject 1").content("Content 1").serviceAddress(serviceUtil.getServiceAddress()).build(),
                Review.builder().movieId(movie.getMovieId()).reviewId(2).author("Author 2").subject("Subject 2").content("Content 2").serviceAddress(serviceUtil.getServiceAddress()).build(),
                Review.builder().movieId(movie.getMovieId()).reviewId(3).author("Author 2").subject("Subject 3").content("Content 3").serviceAddress(serviceUtil.getServiceAddress()).build()
        );
    }

    private List<Recommendation> getRecommendations(Movie movie) {
        return Arrays.asList(
                Recommendation.builder().movieId(movie.getMovieId()).recommendationId(1).author("Author 1").rate(1).content("Content 1").serviceAddress(serviceUtil.getServiceAddress()).build(),
                Recommendation.builder().movieId(movie.getMovieId()).recommendationId(2).author("Author 2").rate(2).content("Content 2").serviceAddress(serviceUtil.getServiceAddress()).build(),
                Recommendation.builder().movieId(movie.getMovieId()).recommendationId(3).author("Author 3").rate(3).content("Content 3").serviceAddress(serviceUtil.getServiceAddress()).build()
        );
    }

}
