package com.geborskimateusz.microservices.composite.movie.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = {
        "app.movie-service.host=7001",
        "app.movie-service.port=localhost",
        "app.recommendation-service.host=7002",
        "app.recommendation-service.port=localhost",
        "app.review-service.host=7001",
        "app.review-service.port=localhost"
})
class MovieCompositeIntegrationTest {

    @Mock
    RestTemplate restTemplate;

    @Mock
    ObjectMapper mapper;

    @Mock
    String movieServiceUrl;

    @Mock
    String recommendationServiceUrl;

    @Mock
    String reviewServiceUrl;

    @InjectMocks
    MovieCompositeIntegration movieCompositeIntegration;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getMovie() {
    }

    @Test
    void getRecommendations() {
    }

    @Test
    void getReviews() {
    }
}