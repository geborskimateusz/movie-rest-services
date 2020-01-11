package com.geborskimateusz.microservices.composite.movie.config;

import com.geborskimateusz.microservices.composite.movie.services.MovieCompositeIntegration;
import org.springframework.boot.actuate.health.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;

@Configuration
public class HealthConfig {

    private final HealthAggregator healthAggregator;
    private final MovieCompositeIntegration movieCompositeIntegration;

    public HealthConfig(HealthAggregator healthAggregator, MovieCompositeIntegration movieCompositeIntegration) {
        this.healthAggregator = healthAggregator;
        this.movieCompositeIntegration = movieCompositeIntegration;
    }

    @Bean
    ReactiveHealthIndicator coreServices() {
        ReactiveHealthIndicatorRegistry registry = new
                DefaultReactiveHealthIndicatorRegistry(new LinkedHashMap<>());

        registry.register("movie", movieCompositeIntegration::getMovieHealth);
        registry.register("recommendations", movieCompositeIntegration::getRecommendationHealth);
        registry.register("reviews", movieCompositeIntegration::getReviewHealth);

        return new CompositeReactiveHealthIndicator(healthAggregator, registry);
    }
}
