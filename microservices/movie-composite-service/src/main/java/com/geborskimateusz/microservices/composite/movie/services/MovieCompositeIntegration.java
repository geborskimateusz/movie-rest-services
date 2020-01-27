package com.geborskimateusz.microservices.composite.movie.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.movie.MovieService;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.recommendation.RecommendationService;
import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.api.core.review.ReviewService;
import com.geborskimateusz.api.event.Event;
import com.geborskimateusz.microservices.composite.movie.services.utils.MessageSources;
import com.geborskimateusz.util.exceptions.InvalidInputException;
import com.geborskimateusz.util.exceptions.NotFoundException;
import com.geborskimateusz.util.http.HttpErrorInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;


/**
 * ParameterizedTypeReference is used here , because of List<T>.
 * RestTemplate can figure out what class to map the JSON responses to.
 **/

@Slf4j
@EnableBinding(MessageSources.class)
@Component
public class MovieCompositeIntegration implements MovieService, RecommendationService, ReviewService {

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final String movieServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    private final MessageSources messageSources;

    @Autowired
    public MovieCompositeIntegration(
            WebClient.Builder webClient,
            MessageSources messageSources,
            ObjectMapper mapper,

            @Value("${app.movie-service.host}") String movieServiceHost,
            @Value("${app.movie-service.port}") int movieServicePort,

            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,

            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort
    ) {
        this.webClient = webClient.build();
        this.messageSources = messageSources;
        this.mapper = mapper;

        movieServiceUrl = "http://" + movieServiceHost + ":" + movieServicePort + "/movie";
        recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation";
        reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
    }

    @Override
    public Mono<Movie> getMovie(Integer movieId) {

        String url = movieServiceUrl + movieId;

        log.debug("Will call getMovie API on URL: {}", url);

        return webClient
                .get().uri(url)
                .retrieve()
                .bodyToMono(Movie.class)
                .log()
                .onErrorMap(WebClientResponseException.class, this::handleHttpClientException);
    }


    @Override
    public Movie createMovie(Movie movie) {

        messageSources.outputMovies()
                .send(MessageBuilder.withPayload(
                        Event.builder()
                                .eventType(Event.Type.CREATE)
                                .key(movie.getMovieId())
                                .data(movie)
                                .build()
                ).build());

        return movie;
    }

    @Override
    public void deleteMovie(Integer movieId) {
        messageSources.outputMovies()
                .send(MessageBuilder.withPayload(
                        Event.builder()
                                .eventType(Event.Type.DELETE)
                                .key(movieId)
                                .build())
                        .build());
    }

    @Override
    public Flux<Recommendation> getRecommendations(int movieId) {

        String url = recommendationServiceUrl + "?movieId=" + movieId;

        log.debug("Will call the getRecommendations API on URL: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Recommendation.class)
                .log()
                .onErrorResume(error -> Flux.empty());
    }

    @Override
    public Recommendation createRecommendation(Recommendation recommendation) {
        messageSources.outputReviews()
                .send(MessageBuilder.withPayload(
                        Event.builder()
                                .eventType(Event.Type.CREATE)
                                .key(recommendation.getRecommendationId())
                                .data(recommendation)
                                .build()
                ).build());

        return recommendation;
    }

    @Override
    public void deleteRecommendations(int movieId) {
        messageSources.outputRecommendations()
                .send(MessageBuilder.withPayload(
                        Event.builder()
                                .eventType(Event.Type.DELETE)
                                .key(movieId)
                                .build())
                        .build());
    }

    @Override
    public Flux<Review> getReviews(int movieId) {
        String url = reviewServiceUrl + "?movieId=" + movieId;

        log.debug("Will call the getReviews API on URL: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .log()
                .onErrorResume(error -> Flux.empty());
    }

    @Override
    public Review createReview(Review review) {
        messageSources.outputReviews()
                .send(MessageBuilder.withPayload(
                        Event.builder()
                                .eventType(Event.Type.CREATE)
                                .key(review.getReviewId())
                                .data(review)
                                .build()
                ).build());

        return review;
    }

    @Override
    public void deleteReviews(int movieId) {
        messageSources.outputReviews()
                .send(MessageBuilder.withPayload(
                        Event.builder()
                                .eventType(Event.Type.DELETE)
                                .key(movieId)
                                .build())
                        .build());
    }

    public Mono<Health> getMovieHealth() {
        return getHealth(movieServiceUrl);
    }

    public Mono<Health> getRecommendationHealth() {
        return getHealth(recommendationServiceUrl);
    }

    public Mono<Health> getReviewHealth() {
        return getHealth(reviewServiceUrl);
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        log.info("Will call the Health API on URL: {}", url);
        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log();
    }


    private Throwable handleHttpClientException(WebClientResponseException ex) {
        if (!(ex instanceof WebClientResponseException)) {
            log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException) ex;

        switch (wcre.getStatusCode()) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));

            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(wcre));

            default:
                log.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                log.warn("Error body: {}", wcre.getResponseBodyAsString());
                return wcre;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

}
