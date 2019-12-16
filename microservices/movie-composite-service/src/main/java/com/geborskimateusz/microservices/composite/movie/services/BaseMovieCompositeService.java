package com.geborskimateusz.microservices.composite.movie.services;

import com.geborskimateusz.api.composite.movie.*;
import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.microservices.composite.movie.services.utils.CompositeAggregator;
import com.geborskimateusz.util.exceptions.NotFoundException;
import com.geborskimateusz.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class BaseMovieCompositeService implements MovieCompositeService {

    private final MovieCompositeIntegration movieCompositeIntegration;
    private final ServiceUtil serviceUtil;

    @Autowired
    public BaseMovieCompositeService(MovieCompositeIntegration movieCompositeIntegration, ServiceUtil serviceUtil) {
        this.movieCompositeIntegration = movieCompositeIntegration;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public MovieAggregate getCompositeMovie(int movieId) {
        Movie movie = movieCompositeIntegration.getMovie(movieId);

        if (movie == null) throw new NotFoundException("No movie found for movieId: " + movieId);

        List<Recommendation> recommendations = movieCompositeIntegration.getRecommendations(movieId);
        List<Review> reviews = movieCompositeIntegration.getReviews(movieId);

        return CompositeAggregator.createMovieAggregate(movie, recommendations, reviews, serviceUtil.getServiceAddress());

    }

    @Override
    public void createCompositeMovie(MovieAggregate body) {
        try {

            log.debug("createCompositeMovie: Trying to create new Movie Entity for movieId: {} ", body.getMovieId());

            createMovieFromBody(body);
            createRecommendationsFromBody(body);
            createReviewsFromBody(body);

        }catch (RuntimeException ex) {
            log.warn("createCompositeMovie failed", ex);
            throw ex;
        }
    }

    @Override
    public void deleteCompositeMovie(int movieId) {

    }


    private void createReviewsFromBody(MovieAggregate body) {
        if (body.getReviews() != null) {
            body.getReviews().forEach(reviewSummary -> {
                Review review = Review.builder()
                        .reviewId(reviewSummary.getReviewId())
                        .movieId(body.getMovieId())
                        .subject(reviewSummary.getSubject())
                        .content(reviewSummary.getContent())
                        .author(reviewSummary.getAuthor())
                        .serviceAddress(null)
                        .build();

                movieCompositeIntegration.createReview(review);
            });
        }
    }

    private void createRecommendationsFromBody(MovieAggregate body) {
        if (body.getRecommendations() != null) {
            body.getRecommendations().forEach(recommendationSummary -> {
                Recommendation recommendation = Recommendation.builder()
                        .recommendationId(recommendationSummary.getRecommendationId())
                        .movieId(body.getMovieId())
                        .author(recommendationSummary.getAuthor())
                        .content(recommendationSummary.getContent())
                        .rate(recommendationSummary.getRate())
                        .serviceAddress(null)
                        .build();

                movieCompositeIntegration.createRecommendation(recommendation);
            });
        }
    }

    private void createMovieFromBody(MovieAggregate body) {
        Movie movie = Movie.builder()
                .movieId(body.getMovieId())
                .genre(body.getGenre())
                .title(body.getTitle())
                .address(null)
                .build();

        movieCompositeIntegration.createMovie(movie);
    }
}
