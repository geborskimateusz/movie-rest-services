package com.geborskimateusz.microservices.core.movie.service;

import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.movie.MovieService;
import com.geborskimateusz.api.event.Event;
import com.geborskimateusz.util.exceptions.EventProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

@Slf4j
@EnableBinding(Sink.class)
public class MessageProcessor {

    private final MovieService movieService;

    public MessageProcessor(MovieService movieService) {
        this.movieService = movieService;
    }

    @StreamListener(Sink.INPUT)
    public void process(Event<Integer, Movie> event) {
        log.info("Process message created at {}...", event.getEventCreatedAt());

        switch (event.getEventType()) {

            case CREATE:
                Movie movie = event.getData();
                log.info("Create movie with ID: {}", movie.getMovieId());
                log.info(movie.toString());
                movieService.createMovie(movie);
                break;

            case DELETE:
                Integer movieId = event.getKey();
                log.info("Delete movie with ID: {}", movieId);
                movieService.deleteMovie(movieId);
                break;

            default:
                String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
                log.warn(errorMessage);
                throw new EventProcessingException(errorMessage);
        }

        log.info("Message processing done!");
    }
}
