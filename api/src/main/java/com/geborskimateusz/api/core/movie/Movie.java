package com.geborskimateusz.api.core.movie;

import lombok.*;

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

    @Override
    public String toString() {
        return "Movie{" +
                "movieId=" + movieId +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
