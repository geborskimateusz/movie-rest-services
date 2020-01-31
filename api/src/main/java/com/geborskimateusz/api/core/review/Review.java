package com.geborskimateusz.api.core.review;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Review {
    private int movieId;
    private int reviewId;
    private String author;
    private String subject;
    private String content;
    private String serviceAddress;

    public Review() {
        movieId = 0;
        reviewId = 0;
        author = null;
        subject = null;
        content = null;
        serviceAddress = null;
    }

    @Override
    public String toString() {
        return "Review{" +
                "movieId=" + movieId +
                ", reviewId=" + reviewId +
                ", author='" + author + '\'' +
                ", subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                ", serviceAddress='" + serviceAddress + '\'' +
                '}';
    }
}