package com.geborskimateusz.microservices.composite.movie.services;

import com.geborskimateusz.api.composite.movie.MovieAggregate;
import com.geborskimateusz.api.composite.movie.MovieCompositeService;
import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.microservices.composite.movie.services.utils.CompositeAggregator;
import com.geborskimateusz.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

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
    public Mono<MovieAggregate> getCompositeMovie(Integer movieId) {
        log.debug("getCompositeMovie: lookup a movie aggregate for movieId: {}", movieId);

        return
                Mono.zip(
                        values -> CompositeAggregator.createMovieAggregate(
                                (Movie) values[0],
                                (List<Recommendation>) values[1],
                                (List<Review>) values[2],
                                serviceUtil.getServiceAddress()),
                        movieCompositeIntegration.getMovie(movieId),
                        movieCompositeIntegration.getRecommendations(movieId).collectList(),
                        movieCompositeIntegration.getReviews(movieId).collectList()
                ).doOnError(ex -> log.warn("getCompositeMovie failed: {}", ex.toString()))
                        .log();

    }

    @Override
    public void createCompositeMovie(MovieAggregate body) {
        try {

            log.info("createCompositeMovie: Trying to create new Movie Entity for movieId: {} ", body.getMovieId());
            log.info(body.toString());

            createMovieFromBody(body);
            createRecommendationsFromBody(body);
            createReviewsFromBody(body);

        } catch (RuntimeException ex) {
            log.warn("createCompositeMovie failed", ex);
            throw ex;
        }
    }

    @Override
    public void deleteCompositeMovie(int movieId) {
        log.debug("deleteCompositeMovie will delete Movie, Reviews, Recommendations belonging to Movie with id: {}", movieId);

        movieCompositeIntegration.deleteMovie(movieId);
        movieCompositeIntegration.deleteReviews(movieId);
        movieCompositeIntegration.deleteRecommendations(movieId);

        log.debug("deleteCompositeMovie deleted Movie, Reviews, Recommendations belonging to Movie with id: {}", movieId);

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

                log.debug("createReviewsFromBody -> reviews size: {}, actual: {}", body.getReviews().size(), review);
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

                log.debug("createRecommendationsFromBody -> recommendations size: {}, actual {}", body.getRecommendations().size(), recommendation);
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

        log.debug("createMovieFromBody, movieId: {}", movie.getMovieId());
        movieCompositeIntegration.createMovie(movie);
    }
}
