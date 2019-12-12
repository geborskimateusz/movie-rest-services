package com.geborskimateusz.api.core.recommendation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Recommendation {
    private final Integer movieId;
    private final Integer recommendationId;
    private final String author;
    private final Integer rate;
    private final String content;
    private String serviceAddress;

    public Recommendation() {
        movieId = 0;
        recommendationId = 0;
        author = null;
        rate = 0;
        content = null;
        serviceAddress = null;
    }
}
