package com.geborskimateusz.api.core.recommendation;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
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
