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
    public Mono<Movie> getMovie(Integer movieId) {

        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);

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
        MovieEntity movieEntity = movieMapper.apiToEntity(movie);

        return movieRepository.save(movieEntity)
                .onErrorMap(DuplicateKeyException.class, ex -> new InvalidInputException("Duplicate key for movieId: " + movie.getMovieId()))
                .log()
                .map(movieMapper::entityToApi).block();
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
}
