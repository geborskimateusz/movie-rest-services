package com.geborskimateusz.microservices.core.recommendation.service;


import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.movie.MovieService;
import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.recommendation.RecommendationService;
import com.geborskimateusz.api.event.Event;
import com.geborskimateusz.util.exceptions.EventProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@Slf4j
@EnableBinding(Sink.class)
public class MessageProcessor {

    private final RecommendationService recommendationService;

    public MessageProcessor(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @StreamListener(Sink.INPUT)
    public void process(Event<Integer, Recommendation> event) {
        log.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

            case CREATE:
                Recommendation recommendation = event.getData();
                log.info("Create recommendation with ID: {}", recommendation.getRecommendationId());
                recommendationService.createRecommendation(recommendation);
                break;

            case DELETE:
                Integer movieId = event.getKey();
                log.info("Delete recommendations for movie with ID: {}", movieId);
                recommendationService.deleteRecommendations(movieId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                log.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        log.info("Message processing done!");
    }
}
