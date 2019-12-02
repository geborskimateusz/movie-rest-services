package com.geborskimateusz.microservices.core.review.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "reviews_unique_idx", unique = true, columnList = "movieId,reviewId")
})
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
