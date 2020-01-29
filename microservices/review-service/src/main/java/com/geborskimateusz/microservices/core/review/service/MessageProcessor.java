package com.geborskimateusz.microservices.core.review.service;

import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.api.core.review.ReviewService;
import com.geborskimateusz.api.event.Event;
import com.geborskimateusz.util.exceptions.EventProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@Slf4j
@EnableBinding(Sink.class)
public class MessageProcessor {

    private final ReviewService reviewService;

    public MessageProcessor(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @StreamListener(target = Sink.INPUT)
    public void process(Event<Integer, Review> event) {

        log.info("Process message created at {}...", event.getEventCreatedAt());
        log.info("Process message body: {}", event.toString());

        switch (event.getEventType()) {

            case CREATE:
                Review review = event.getData();
                log.info("Create review with ID: {}/{}", review.getMovieId(), review.getReviewId());
                log.info(review.toString());
                reviewService.createReview(review);
                break;

            case DELETE:
                int movieId = event.getKey();
                log.info("Delete reviews with movieID: {}", movieId);
                reviewService.deleteReviews(movieId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                log.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        log.info("Message processing done!");
    }
}