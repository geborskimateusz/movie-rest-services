package com.geborskimateusz.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;

@Slf4j
@Configuration
public class HealthCheckConfiguration {

    private final String AUTH_SERVICE_URL = "http://auth-server";
    private final String MOVIE_SERVICE_URL = "http://movie";
    private final String RECOMMENDATION_SERVICE_URL = "http://recommendation";
    private final String REVIEW_SERVICE_URL = "http://review";
    private final String MOVIE_COMPOSITE_SERVICE_URL = "http://movie-composite";

    private HealthAggregator healthAggregator;
    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;

    public HealthCheckConfiguration(HealthAggregator healthAggregator, WebClient.Builder webClientBuilder) {
        this.healthAggregator = healthAggregator;
        this.webClientBuilder = webClientBuilder;
    }

    @Bean
    ReactiveHealthIndicator coreServices() {
        ReactiveHealthIndicatorRegistry registry = new
                DefaultReactiveHealthIndicatorRegistry(new LinkedHashMap<>());

        registry.register("auth-server", this::getAuthHealth);
        registry.register("movie", this::getMovieHealth);
        registry.register("recommendations", this::getRecommendationHealth);
        registry.register("reviews", this::getReviewHealth);
        registry.register("movie-composite", this::getMovieCompositeHealth);

        return new CompositeReactiveHealthIndicator(healthAggregator, registry);
    }

    private Mono<Health> getMovieCompositeHealth() {
        return getHealth(MOVIE_SERVICE_URL);
    }

    public Mono<Health> getAuthHealth() {
        return getHealth(AUTH_SERVICE_URL);
    }

    public Mono<Health> getMovieHealth() {
        return getHealth(MOVIE_SERVICE_URL);
    }

    public Mono<Health> getRecommendationHealth() {
        return getHealth(RECOMMENDATION_SERVICE_URL);
    }

    public Mono<Health> getReviewHealth() {
        return getHealth(REVIEW_SERVICE_URL);
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        log.info("Will call the Health API on URL: {}", url);
        return getWebClient().get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log();
    }

    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }

        return webClient;
    }
}
