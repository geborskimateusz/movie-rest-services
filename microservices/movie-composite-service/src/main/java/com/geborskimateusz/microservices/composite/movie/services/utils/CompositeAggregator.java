package com.geborskimateusz.microservices.composite.movie.services.utils;

import com.geborskimateusz.api.composite.movie.MovieAggregate;
import com.geborskimateusz.api.composite.movie.RecommendationSummary;
import com.geborskimateusz.api.composite.movie.ReviewSummary;
import com.geborskimateusz.api.composite.movie.ServiceAddresses;
import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.review.Review;

import java.util.List;
import java.util.stream.Collectors;

public class CompositeAggregator {

    public static MovieAggregate createMovieAggregate(Movie movie, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {

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
