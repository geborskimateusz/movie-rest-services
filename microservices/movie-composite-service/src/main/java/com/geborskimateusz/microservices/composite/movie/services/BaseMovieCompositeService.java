package com.geborskimateusz.microservices.composite.movie.services;

import com.geborskimateusz.api.composite.movie.*;
import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.util.exceptions.NotFoundException;
import com.geborskimateusz.util.http.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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
    public MovieAggregate getMovie(int movieId) {

        Movie movie = movieCompositeIntegration.getMovie(movieId);
        if (movie == null) throw new NotFoundException("No movie found for movieId: " + movieId);

        List<Recommendation> recommendations = movieCompositeIntegration.getRecommendations(movieId);
        List<Review> reviews = movieCompositeIntegration.getReviews(movieId);

        return createMovieAggregate(movie, recommendations, reviews, serviceUtil.getServiceAddress());

    }

    private MovieAggregate createMovieAggregate(Movie movie, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {

        int movieId = movie.getMovieId();
        String title = movie.getTitle();
        String genre = movie.getGenre();

        List<RecommendationSummary> recommendationSummaries =
                (recommendations == null) ? null : recommendations.stream().map(
                        r -> RecommendationSummary.builder()
                                .recommendationId(r.getRecommendationId())
                                .author(r.getAuthor())
                                .rate(r.getRate())
                                .build())
                        .collect(Collectors.toList());

        List<ReviewSummary> reviewSummaries =
                (reviews == null) ? null : reviews.stream().map(
                        r -> ReviewSummary.builder()
                                .reviewId(r.getReviewId())
                                .author(r.getAuthor())
                                .subject(r.getSubject())
                                .build())
                        .collect(Collectors.toList());


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
