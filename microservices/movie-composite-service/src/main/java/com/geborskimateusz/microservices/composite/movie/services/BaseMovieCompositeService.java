package com.geborskimateusz.microservices.composite.movie.services;

import com.geborskimateusz.api.composite.movie.*;
import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.review.Review;
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

        return createMovieAggregate(movie, recommendations, reviews, serviceUtil.getServiceAddress());

    }

    @Override
    public void createCompositeMovie(MovieAggregate body) {
        try {
            log.debug("createCompositeMovie: Trying to create new Movie Entity for movieId: {} ", body.getMovieId());

            Movie movie = Movie.builder()
                    .movieId(body.getMovieId())
                    .genre(body.getGenre())
                    .title(body.getTitle())
                    .address(null)
                    .build();

            movieCompositeIntegration.createMovie(movie);

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(recommendationSummary -> {
                    Recommendation recommendation = Recommendation.builder()
                            .recommendationId(recommendationSummary.getRecommendationId())
                            .movieId(movie.getMovieId())
                            .author(recommendationSummary.getAuthor())
                            .content(recommendationSummary.getContent())
                            .rate(recommendationSummary.getRate())
                            .serviceAddress(null)
                            .build();

                    movieCompositeIntegration.createRecommendation(recommendation);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(reviewSummary -> {
                    Review review = Review.builder()
                            .reviewId(reviewSummary.getReviewId())
                            .movieId(movie.getMovieId())
                            .subject(reviewSummary.getSubject())
                            .content(reviewSummary.getContent())
                            .author(reviewSummary.getAuthor())
                            .serviceAddress(null)
                            .build();

                    movieCompositeIntegration.createReview(review);
                });
            }

        }catch (RuntimeException ex) {
            log.warn("createCompositeMovie failed", ex);
            throw ex;
        }
    }

    @Override
    public void deleteCompositeMovie(int movieId) {

    }

    private MovieAggregate createMovieAggregate(Movie movie, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {

        // 1. Setup movie info
        int movieId = movie.getMovieId();
        String title = movie.getTitle();
        String genre = movie.getGenre();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries =
                (recommendations == null) ? null : recommendations.stream().map(
                        r -> RecommendationSummary.builder()
                                .recommendationId(r.getRecommendationId())
                                .author(r.getAuthor())
                                .rate(r.getRate())
                                .build())
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries =
                (reviews == null) ? null : reviews.stream().map(
                        r -> ReviewSummary.builder()
                                .reviewId(r.getReviewId())
                                .author(r.getAuthor())
                                .subject(r.getSubject())
                                .build())
                        .collect(Collectors.toList());


        // 4. Create info regarding the involved microservices addresses
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = ServiceAddresses.builder()
                .cmp(serviceAddress)
                .mov(movie.getAddress())
                .rev(reviewAddress)
                .rec(recommendationAddress)
                .build();

        return MovieAggregate.builder()
                .movieId(movieId)
                .title(title)
                .genre(genre)
                .recommendations(recommendationSummaries)
                .reviews(reviewSummaries)
                .serviceAddresses(serviceAddresses)
                .build();
    }
}
