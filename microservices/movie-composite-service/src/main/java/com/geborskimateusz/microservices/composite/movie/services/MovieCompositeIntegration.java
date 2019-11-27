package com.geborskimateusz.microservices.composite.movie.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.movie.MovieService;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.recommendation.RecommendationService;
import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.api.core.review.ReviewService;
import com.geborskimateusz.util.exceptions.InvalidInputException;
import com.geborskimateusz.util.exceptions.NotFoundException;
import com.geborskimateusz.util.http.HttpErrorInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * ParameterizedTypeReference is used here , because of List<T>.
 * RestTemplate can figure out what class to map the JSON responses to.
 **/

@Slf4j
@Component
public class MovieCompositeIntegration implements MovieService, RecommendationService, ReviewService {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final String movieServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public MovieCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper mapper,

            @Value("${app.movie-service.host}") String movieServiceHost,
            @Value("${app.movie-service.port}") int movieServicePort,

            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,

            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort
    ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;

        movieServiceUrl = "http://" + movieServiceHost + ":" + movieServicePort + "/movie/";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?movieId=";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?movieId=";
    }

    @Override
    public Movie getMovie(int movieId) {
        try {
            String url = movieServiceUrl + movieId;
            log.debug("Will call getMovie API on URL: {}", url);

            Movie movie = restTemplate.getForObject(url, Movie.class);
            log.debug("Found a movie with id: {}", movie.getMovieId());

            return movie;

        } catch (HttpClientErrorException ex) {

            switch (ex.getStatusCode()) {

                case NOT_FOUND:
                    throw new NotFoundException(getErrorMessage(ex));

                case UNPROCESSABLE_ENTITY:
                    throw new InvalidInputException(getErrorMessage(ex));

                default:
                    log.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                    log.warn("Error body: {}", ex.getResponseBodyAsString());
                    throw ex;
            }
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }


    @Override
    public List<Recommendation> getRecommendations(int movieId) {

        try {

            String url = recommendationServiceUrl + movieId;

            log.debug("Will call getRecommendations API on URL: {}", url);
            List<Recommendation> recommendations = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Recommendation>>() {
                    }).getBody();

            log.debug("Found {} recommendations for a movie with id: {}", recommendations.size(), movieId);
            return recommendations;


        } catch (Exception ex) {
            log.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Review> getReviews(int movieId) {
        try {
            String url = reviewServiceUrl + movieId;

            log.debug("Will call getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Review>>() {
                    }).getBody();

            log.debug("Found {} reviews for a movei with id: {}", reviews.size(), movieId);
            return reviews;

        } catch (Exception ex) {
            log.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }
}
