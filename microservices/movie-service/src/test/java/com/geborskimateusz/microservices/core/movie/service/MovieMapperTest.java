package com.geborskimateusz.microservices.core.movie.service;

import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.microservices.core.movie.persistence.MovieEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovieMapperTest {

    public static final String FAKE_GENRE = "Fake genre";
    public static final String FAKE_ADDRESS = "Fake address";
    public static final int MOVIE_ID = 1;
    public static final String FAKE_TITLE = "Fake title";
    public static final String ID = "1";
    public static final int VERSION = 123;

    MovieMapper movieMapper = MovieMapper.INSTANCE;

    @Test
    void entityToApi() {

        MovieEntity movieEntity = MovieEntity.builder()
                .genre(FAKE_GENRE)
                .address(FAKE_ADDRESS)
                .movieId(MOVIE_ID)
                .title(FAKE_TITLE)
                .build();
        movieEntity.setId(ID);
        movieEntity.setVersion(VERSION);

        Movie movie = movieMapper.entityToApi(movieEntity);

        assertMapper(movie, movieEntity);
        assertNull(movie.getAddress());
    }

    @Test
    void apiToEntity() {

        MovieEntity movieEntity = MovieEntity.builder()
                .genre(FAKE_GENRE)
                .address(FAKE_ADDRESS)
                .movieId(MOVIE_ID)
                .title(FAKE_TITLE)
                .build();
        movieEntity.setId(ID);
        movieEntity.setVersion(VERSION);

        Movie movie = movieMapper.entityToApi(movieEntity);

        assertMapper(movie, movieEntity);
    }

    private void assertMapper(Movie movie, MovieEntity movieEntity ) {
        assertNotNull(movie);
        assertNotNull(movieEntity);
        assertEquals(movie.getMovieId(), (int) movieEntity.getMovieId());
        assertEquals(movie.getGenre(), movieEntity.getGenre());
        assertEquals(movie.getTitle(), movieEntity.getTitle());
    }
}