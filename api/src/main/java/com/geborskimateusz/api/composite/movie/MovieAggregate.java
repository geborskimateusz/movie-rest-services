package com.geborskimateusz.api.composite.movie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MovieAggregate {
    private final int movieId;
    private final String title;
    private final String genre;
    private final String address;
    private final List<RecommendationSummary> recommendations;
    private final List<ReviewSummary> reviews;
    private final ServiceAddresses serviceAddresses;

}
