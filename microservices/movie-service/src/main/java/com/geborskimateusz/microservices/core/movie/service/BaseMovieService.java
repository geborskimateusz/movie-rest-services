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

@Slf4j
@RestController
public class BaseMovieService implements MovieService {

    private final ServiceUtil serviceUtil;
    private final MovieRepository movieRepository;

    private final MovieMapper movieMapper = MovieMapper.INSTANCE;

    @Autowired
    public BaseMovieService(ServiceUtil serviceUtil, MovieRepository movieRepository) {
        this.serviceUtil = serviceUtil;
        this.movieRepository = movieRepository;
    }


    @Override
    public Movie getMovie(Integer movieId) {

        if (movieId < 1) throw new InvalidInputException("Invalid productId: " + movieId);

        MovieEntity movieEntity = movieRepository.findMovieById(movieId)
                .orElseThrow(() -> new NotFoundException("No product found for productId: " + movieId));

        Movie movie = movieMapper.entityToApi(movieEntity);
        movie.setAddress(serviceUtil.getServiceAddress());

        log.debug("/movie return the found movie for movieId={}", movieId);

        return movie;
    }

    @Override
    public Movie createMovie(Movie movie) {

        try {

            MovieEntity movieEntity = movieMapper.apiToEntity(movie);
            MovieEntity saved =  movieRepository.save(movieEntity);

            log.debug("createMovie: entity created for movieId: {}", movie.getMovieId());
            return movieMapper.entityToApi(saved);
        }catch (DuplicateKeyException e) {
            throw new InvalidInputException("Duplicate key for movieId: " +movie.getMovieId());
        }

    }

    @Override
    public void deleteMovie(Integer movieId) {

    }
}
