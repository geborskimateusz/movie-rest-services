package com.geborskimateusz.microservices.core.movie.persistence;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@Getter
@Setter
@Document(collection = "products")
public class MovieEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    @Indexed(unique = true)
    private Integer movieId;

    private String title;
    private String genre;
    private String address;

    @Builder
    public MovieEntity(Integer movieId, String title, String genre, String address) {
        this.movieId = movieId;
        this.title = title;
        this.genre = genre;
        this.address = address;
    }
}
