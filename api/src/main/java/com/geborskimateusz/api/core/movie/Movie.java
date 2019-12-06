package com.geborskimateusz.api.core.movie;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class Movie {
    private int movieId;
    private String title;
    private String genre;
    private String address;

    public Movie() {
        movieId = 0;
        title = null;
        genre = null;
        address = "";
    }
}
