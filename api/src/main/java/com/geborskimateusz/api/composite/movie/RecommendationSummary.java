package com.geborskimateusz.api.composite.movie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RecommendationSummary {
    private final int recommendationId;
    private final String author;
    private final int rate;
}
