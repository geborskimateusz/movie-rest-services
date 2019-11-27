package com.geborskimateusz.microservices.composite.movie.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geborskimateusz.api.core.movie.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    void getRecommendations() {
    }

    @Test
    void getReviews() {
    }
}