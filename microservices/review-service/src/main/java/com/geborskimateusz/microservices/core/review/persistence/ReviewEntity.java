package com.geborskimateusz.microservices.core.review.persistence;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "reviews", indexes = { @Index(name = "reviews_unique_idx", unique = true, columnList = "movieId,reviewId") })
public class ReviewEntity {

    @Id
    @GeneratedValue
    private Integer id;

    @Version
    private Integer version;

    private Integer movieId;
    private Integer reviewId;
    private String author;
    private String subject;
    private String content;
    private String serviceAddress;

}
