package com.geborskimateusz.microservices.composite.movie.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class MovieCompositeIntegrationTest {


    @Mock
    RestTemplate restTemplate;

    @Mock
    ObjectMapper mapper;

    String movieServiceUrl = "http://localhost:7001/movie/";
    String recommendationServiceUrl = "http://localhost:7002/recommendation?movieId=";
    String reviewServiceUrl = "http://localhost:7003/review?movieId=";

    MovieCompositeIntegration movieCompositeIntegration;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        movieCompositeIntegration = new MovieCompositeIntegration(
                restTemplate,
                mapper,
                "localhost",7001,
                "localhost",7002,
                "localhost",7003);
    }

    @Test
    void getMovie() {
        int given = 1;

        Movie movie = Movie.builder().movieId(given).address("Fake address").genre("Fake genre").title("Fake title").build();

        when(restTemplate.getForObject(movieServiceUrl+given, Movie.class)).thenReturn(movie);

        Movie returned = movieCompositeIntegration.getMovie(given);

        assertNotNull(returned);
        assertEquals(movie.getMovieId(),returned.getMovieId());
        assertEquals(movie.getTitle(),returned.getTitle());
        assertEquals(movie.getAddress(),returned.getAddress());
        assertEquals(movie.getGenre(),returned.getGenre());
    }

    @Test
    void createMovie() {
        int given = 1;

        Movie movie = Movie.builder().movieId(given).address("Fake address").genre("Fake genre").title("Fake title").build();

        when(restTemplate.postForObject(movieServiceUrl,movie, Movie.class)).thenReturn(movie);

        Movie returned = movieCompositeIntegration.createMovie(movie);

        assertNotNull(returned);
        assertEquals(movie.getMovieId(),returned.getMovieId());
        assertEquals(movie.getTitle(),returned.getTitle());
        assertEquals(movie.getAddress(),returned.getAddress());
        assertEquals(movie.getGenre(),returned.getGenre());
    }
}