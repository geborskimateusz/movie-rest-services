package com.geborskimateusz.microservices.composite.movie.services;

import com.geborskimateusz.api.composite.movie.MovieAggregate;
import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.microservices.composite.movie.services.utils.CompositeAggregator;
import com.geborskimateusz.util.exceptions.NotFoundException;
import com.geborskimateusz.util.http.ServiceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

class BaseMovieCompositeServiceTest {

    @Mock
    MovieCompositeIntegration movieCompositeIntegration;

    @Mock
    ServiceUtil serviceUtil;

    @InjectMocks
    BaseMovieCompositeService movieCompositeService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getCompositeMovie() {

        int given = 1;

        Movie movie = getMovie(given);

        when(serviceUtil.getServiceAddress()).thenReturn("Fake service address");

        List<Recommendation> recommendations = getRecommendations(movie);
        List<Review> reviews = getReviews(movie);

        when(movieCompositeIntegration.getMovie(given)).thenReturn(Mono.just(movie));
        when(movieCompositeIntegration.getRecommendations(movie.getMovieId())).thenReturn(Flux.fromIterable(recommendations));
        when(movieCompositeIntegration.getReviews(movie.getMovieId())).thenReturn(Flux.fromIterable(reviews));

        MovieAggregate movieAggregate = movieCompositeService.getCompositeMovie(given).block();

        assertNotNull(movieAggregate);
        assertAll(() -> {
            assertEquals(movie.getMovieId(), movieAggregate.getMovieId());
            assertEquals(movie.getTitle(), movieAggregate.getTitle());
            assertFalse(movieAggregate.getRecommendations().isEmpty());
            assertFalse(movieAggregate.getReviews().isEmpty());
        });
    }

    @Test
    void getCompositeMovieShouldThrowNotFoundException() {
        int given = 1;

        when(movieCompositeIntegration.getMovie(anyInt())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> movieCompositeService.getCompositeMovie(given));
    }


    @Test
    void createCompositeMovie() {
        int movieId = 1;
        Movie movie = getMovie(movieId);
        List<Recommendation> recommendations = getRecommendations(movie);
        List<Review> reviews = getReviews(movie);

        MovieAggregate given = CompositeAggregator.createMovieAggregate(movie, recommendations, reviews, null);

        movieCompositeService.createCompositeMovie(given);

        verify(movieCompositeIntegration, times(1)).createMovie(any(Movie.class));
        verify(movieCompositeIntegration, times(recommendations.size())).createRecommendation(any(Recommendation.class));
        verify(movieCompositeIntegration, times(reviews.size())).createReview(any(Review.class));
    }

    @Test
    void createCompositeMovieThrowsRuntimeException() {
        int movieId = 1;
        Movie movie = getMovie(movieId);
        List<Recommendation> recommendations = getRecommendations(movie);
        List<Review> reviews = getReviews(movie);

        MovieAggregate given = CompositeAggregator.createMovieAggregate(movie, recommendations, reviews, null);

        willThrow(RuntimeException.class).given(movieCompositeIntegration).createMovie(any());
        assertThrows(RuntimeException.class, () -> movieCompositeService.createCompositeMovie(given));
    }

    @Test
    void deleteCompositeMovie() {
        int movieId = 1;
        Movie movie = getMovie(movieId);
        List<Recommendation> recommendations = getRecommendations(movie);
        List<Review> reviews = getReviews(movie);

        MovieAggregate given = CompositeAggregator.createMovieAggregate(movie, recommendations, reviews, null);
        movieCompositeService.createCompositeMovie(given);


        movieCompositeService.deleteCompositeMovie(movieId);

        verify(movieCompositeIntegration, times(1)).deleteMovie(given.getMovieId());
        verify(movieCompositeIntegration, times(1)).deleteReviews(given.getMovieId());
        verify(movieCompositeIntegration, times(1)).deleteRecommendations(given.getMovieId());
    }



    private Movie getMovie(int movieId) {
        return Movie.builder().movieId(movieId).address("Fake address").genre("Fake genre").title("Fake title").build();
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