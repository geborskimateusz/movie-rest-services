package com.geborskimateusz.microservices.core.recommendation.persistence;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection="recommendations")
@CompoundIndex(name = "mov-rec-id", unique = true, def = "{'movieId': 1, 'recommendationId' : 1}")
public class RecommendationEntity {
    @Id
    private String id;

    @Version
    private Integer version;

    private Integer movieId;
    private Integer recommendationId;
    private String author;
    private Integer rate;
    private String content;
    private String serviceAddress;

    @Builder
    public RecommendationEntity(Integer movieId, Integer recommendationId, String author, Integer rate, String content, String serviceAddress) {
        this.movieId = movieId;
        this.recommendationId = recommendationId;
        this.author = author;
        this.rate = rate;
        this.content = content;
        this.serviceAddress = serviceAddress;
    }
}
