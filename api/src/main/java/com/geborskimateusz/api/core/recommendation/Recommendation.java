package com.geborskimateusz.api.core.recommendation;

import lombok.*;

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

    @Override
    public String toString() {
        return "Recommendation{" +
                "movieId=" + movieId +
                ", recommendationId=" + recommendationId +
                ", author='" + author + '\'' +
                ", rate=" + rate +
                ", content='" + content + '\'' +
                ", serviceAddress='" + serviceAddress + '\'' +
                '}';
    }
}
