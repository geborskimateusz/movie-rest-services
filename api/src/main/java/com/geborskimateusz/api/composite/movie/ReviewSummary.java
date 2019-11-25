package com.geborskimateusz.api.composite.movie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReviewSummary {
    private final int reviewId;
    private final String author;
    private final String subject;
}
