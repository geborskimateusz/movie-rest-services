package com.geborskimateusz.microservices.core.movie.service;

import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.movie.MovieService;
import com.geborskimateusz.microservices.core.movie.persistence.MovieEntity;
import com.geborskimateusz.microservices.core.movie.persistence.MovieRepository;
import com.geborskimateusz.util.exceptions.InvalidInputException;
import com.geborskimateusz.util.exceptions.NotFoundException;
import com.geborskimateusz.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Random;

@Slf4j
@RestController
public class BaseMovieService implements MovieService {

    private final ServiceUtil serviceUtil;
    private final MovieRepository movieRepository;

    private final MovieMapper movieMapper = MovieMapper.INSTANCE;

    public BaseMovieService(ServiceUtil serviceUtil, MovieRepository movieRepository) {
        this.serviceUtil = serviceUtil;
        this.movieRepository = movieRepository;
    }


    @Override
    public Mono<Movie> getMovie(Integer movieId, int delay, int faultPercent) {
        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);
        if (delay > 0) simulateDelay(delay);
        if (faultPercent > 0) throwErrorIfBadLuck(faultPercent);

        return movieRepository.findByMovieId(movieId)
                .switchIfEmpty(Mono.error(new NotFoundException("No movie found for movieId: " + movieId)))
                .log()
                .map(movieMapper::entityToApi)
                .map(movie -> {
                    movie.setAddress(serviceUtil.getServiceAddress());
                    log.debug("movie return the found movie for movieId={}", movieId);
                    return movie;
                });
    }

    @Override
    public Movie createMovie(Movie movie) {
        log.info("createMovie: Trying to create Movie Entity, passed argument: {}", movie.toString());

        MovieEntity movieEntity = movieMapper.apiToEntity(movie);

        return movieRepository.save(movieEntity)
                .onErrorMap(DuplicateKeyException.class, ex -> new InvalidInputException("Duplicate key for movieId: " + movie.getMovieId()))
                .log()
                .map(movieMapper::entityToApi)
                .block();
    }

    @Override
    public void deleteMovie(Integer movieId) {
        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);

        movieRepository.findByMovieId(movieId)
                .log()
                .map(movieRepository::delete)
                .flatMap(e -> e)
                .block();
    }


    /**
     * Private methods to simulate CircuitBreaker activation
     */

    private void throwErrorIfBadLuck(int faultPercent) {
        int randomThreshold = getRandomNumber(1, 100);
        if (faultPercent < randomThreshold) {
            log.debug("We got lucky, no error occurred, {} < {}",
                    faultPercent, randomThreshold);
        } else {
            log.debug("Bad luck, an error occurred, {} >= {}",
                    faultPercent, randomThreshold);
            throw new RuntimeException("Something went wrong...");
        }
    }

    private final Random randomNumberGenerator = new Random();

    private int getRandomNumber(int min, int max) {
        if (max < min) {
            throw new RuntimeException("Max must be greater than min");
        }
        return randomNumberGenerator.nextInt((max - min) + 1) + min;
    }

    private void simulateDelay(int delay) {
        log.debug("Sleeping for {} seconds..", delay);
        try {
            Thread.sleep(delay * 1000);
        } catch (InterruptedException e) {
            //do nothing
        }
        log.info("Moving on..");
    }
}
