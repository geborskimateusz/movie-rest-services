package com.geborskimateusz.microservices.core.movie.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
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
}
