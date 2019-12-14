package com.geborskimateusz.microservices.composite.movie.services;

import com.geborskimateusz.api.composite.movie.MovieAggregate;
import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.util.exceptions.NotFoundException;
import com.geborskimateusz.util.http.ServiceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class BaseMovieCompositeServiceTest {

    @Mock
    MovieCompositeIntegration movieCompositeIntegration;

    @Mock
    ServiceUtil serviceUtil;

    @InjectMocks
    BaseMovieCompositeService movieCompositeService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this); //without this you will get NPE
    }

    @Test
    void getMovie() {

        int given = 1;

        Movie movie = Movie.builder().movieId(given).address("Fake address").genre("Fake genre").title("Fake title").build();

        when(serviceUtil.getServiceAddress()).thenReturn("Fake service address");

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

        when(movieCompositeIntegration.getMovie(given)).thenReturn(movie);
        when(movieCompositeIntegration.getRecommendations(movie.getMovieId())).thenReturn(recommendations);
        when(movieCompositeIntegration.getReviews(movie.getMovieId())).thenReturn(reviews);

        MovieAggregate movieAggregate = movieCompositeService.getCompositeMovie(given);

        assertNotNull(movieAggregate);
        assertAll(() -> {
            assertEquals(movie.getMovieId(), movieAggregate.getMovieId());
            assertEquals(movie.getTitle(), movieAggregate.getTitle());
        });
    }

    @Test
    void getMovieShouldThrowNotFoundException() {
        int given = 1;

        when(movieCompositeIntegration.getMovie(anyInt())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> movieCompositeService.getCompositeMovie(given));
    }
}