package com.geborskimateusz.api.core.movie;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Movie {
    private final int movieId;
    private final String title;
    private final String genre;
    private final double length;

    public Movie() {
        movieId = 0;
        title = null;
        genre = null;
        length = 0;
    }
}
