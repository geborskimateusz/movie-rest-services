package com.geborskimateusz.microservices.core.movie.persistence;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Document(collection = "movies")
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
}
