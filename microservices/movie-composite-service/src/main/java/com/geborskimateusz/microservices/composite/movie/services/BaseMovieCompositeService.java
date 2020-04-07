package com.geborskimateusz.microservices.composite.movie.services;

import com.geborskimateusz.api.composite.movie.MovieAggregate;
import com.geborskimateusz.api.composite.movie.MovieCompositeService;
import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.microservices.composite.movie.services.utils.CompositeAggregator;
import com.geborskimateusz.util.exceptions.NotFoundException;
import com.geborskimateusz.util.http.ServiceUtil;
import io.github.resilience4j.reactor.retry.RetryExceptionWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;

@Slf4j
@RestController
public class BaseMovieCompositeService implements MovieCompositeService {

    private final SecurityContext nullSC = new SecurityContextImpl();

    private final MovieCompositeIntegration movieCompositeIntegration;
    private final ServiceUtil serviceUtil;

    @Autowired
    public BaseMovieCompositeService(MovieCompositeIntegration movieCompositeIntegration, ServiceUtil serviceUtil) {
        this.movieCompositeIntegration = movieCompositeIntegration;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<MovieAggregate> getCompositeMovie(Integer movieId, int delay, int faultPercent) {
        log.debug("getCompositeMovie: lookup a movie aggregate for movieId: {}", movieId);

        return
                Mono.zip(
                        values -> CompositeAggregator.createMovieAggregate(
                                (Movie) values[1],
                                (List<Recommendation>) values[2],
                                (List<Review>) values[3],
                                serviceUtil.getServiceAddress()),
                        ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSC),
                        movieCompositeIntegration.getMovie(movieId, delay, faultPercent)
                                .onErrorMap(RetryExceptionWrapper.class, Throwable::getCause)
                                .onErrorReturn(RequestHandlerCircuitBreakerAdvice.CircuitBreakerOpenException.class, getMovieFallbackValue(movieId)),
                        movieCompositeIntegration.getRecommendations(movieId).collectList(),
                        movieCompositeIntegration.getReviews(movieId).collectList()
                ).doOnError(ex -> log.warn("getCompositeMovie failed: {}", ex.toString())).log();

    }

    /**
     * The fallback logic can be based on movieId lookup information on the product from
     * alternative sources, for example, an internal cache. In our case, we return a hardcoded
     * value unless movieId is 13 ; otherwise, we throw a not found exception:
     *
     * @param movieId
     * @return
     */
    private Movie getMovieFallbackValue(Integer movieId) {
        log.warn("Creating fallback movie for movieId {}", movieId);
        if (movieId == 13) {
            String errMsg = "Moviw Id: " + movieId + " not found in fallback cache!";
            log.warn(errMsg);
            throw new NotFoundException(errMsg);
        }
        return Movie.builder()
                .movieId(movieId).genre("Fallback genre").title("Fallback title")
                .address(serviceUtil.getServiceAddress())
                .build();
    }

    @Override
    public Mono<Void> createCompositeMovie(MovieAggregate body) {
        return ReactiveSecurityContextHolder.getContext()
                .doOnSuccess(securityContext -> internalCreateCompositeMovie(securityContext, body))
                .then();
    }

    private void internalCreateCompositeMovie(SecurityContext securityContext, MovieAggregate body) {

        try {

            logAuthorizationInfo(securityContext);

            log.info("createCompositeMovie: Trying to create new Movie Entity for movieId: {} ", body.getMovieId());
            log.info(body.toString());

            createMovieFromBody(body);
            createRecommendationsFromBody(body);
            createReviewsFromBody(body);

        } catch (RuntimeException ex) {
            log.warn("createCompositeMovie failed", ex);
            throw ex;
        }
    }

    @Override
    public Mono<Void> deleteCompositeMovie(int movieId) {

        return ReactiveSecurityContextHolder.getContext()
                .doOnSuccess(securityContext -> internalDeleteCompositeMovie(securityContext, movieId))
                .then();

    }

    private void internalDeleteCompositeMovie(SecurityContext securityContext, int movieId) {

        logAuthorizationInfo(securityContext);

        try {
            log.debug("deleteCompositeMovie will delete Movie, Reviews, Recommendations belonging to Movie with id: {}", movieId);

            movieCompositeIntegration.deleteMovie(movieId);
            movieCompositeIntegration.deleteReviews(movieId);
            movieCompositeIntegration.deleteRecommendations(movieId);

            log.debug("deleteCompositeMovie deleted Movie, Reviews, Recommendations belonging to Movie with id: {}", movieId);
        } catch (RuntimeException ex) {
            log.warn("deleteCompositeMovie failed: {}", ex.toString());
            throw ex;
        }
    }


    private void createReviewsFromBody(MovieAggregate body) {
        if (body.getReviews() != null && !body.getReviews().isEmpty()) {
            body.getReviews().forEach(reviewSummary -> {
                Review review = Review.builder()
                        .reviewId(reviewSummary.getReviewId())
                        .movieId(body.getMovieId())
                        .subject(reviewSummary.getSubject())
                        .content(reviewSummary.getContent())
                        .author(reviewSummary.getAuthor())
                        .serviceAddress(null)
                        .build();

                log.info("createReviewsFromBody -> reviews size: {}, actual: {}", body.getReviews().size(), review);
                movieCompositeIntegration.createReview(review);
            });
        }

        log.info("There are no reviews in MovieAggregate");
    }

    private void createRecommendationsFromBody(MovieAggregate body) {
        if (body.getRecommendations() != null && !body.getRecommendations().isEmpty()) {
            body.getRecommendations().forEach(recommendationSummary -> {
                Recommendation recommendation = Recommendation.builder()
                        .recommendationId(recommendationSummary.getRecommendationId())
                        .movieId(body.getMovieId())
                        .author(recommendationSummary.getAuthor())
                        .content(recommendationSummary.getContent())
                        .rate(recommendationSummary.getRate())
                        .serviceAddress(null)
                        .build();

                log.info("createRecommendationsFromBody -> recommendations size: {}, actual {}", body.getRecommendations().size(), recommendation);
                movieCompositeIntegration.createRecommendation(recommendation);
            });
        }

        log.info("There are no recommendations in MovieAggregate");
    }

    private void createMovieFromBody(MovieAggregate body) {
        Movie movie = Movie.builder()
                .movieId(body.getMovieId())
                .genre(body.getGenre())
                .title(body.getTitle())
                .address(null)
                .build();

        log.debug("createMovieFromBody, movieId: {}", movie.getMovieId());
        movieCompositeIntegration.createMovie(movie);
    }

    private void logAuthorizationInfo(SecurityContext securityContext) {

        if (
            //@formatter:off
                securityContext != null &&
                        securityContext.getAuthentication() != null &&
                        securityContext.getAuthentication() instanceof JwtAuthenticationToken
            //@formatter:on
        ) {
            Jwt jwt = ((JwtAuthenticationToken) securityContext.getAuthentication()).getToken();
            logAuthorizationInfo(jwt);
        } else {
            log.warn("No JWT token supplied.");
        }

    }

    private void logAuthorizationInfo(Jwt jwt) {
        if (jwt == null) {
            log.warn("No JWT token supplied.");
        } else {
            if (log.isDebugEnabled()) {
                URL issuer = jwt.getIssuer();
                List<String> audience = jwt.getAudience();
                Object subject = jwt.getClaims().get("sub");
                Object scopes = jwt.getClaims().get("scope");
                Object expires = jwt.getClaims().get("exp");

                log.debug("Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}", subject, scopes, expires, issuer, audience);
            }
        }
    }
}
