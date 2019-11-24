package com.geborskimateusz.microservices.core.movie.service;

import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.api.core.movie.MovieService;
import com.geborskimateusz.util.exceptions.InvalidInputException;
import com.geborskimateusz.util.exceptions.NotFoundException;
import com.geborskimateusz.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class BaseMovieService implements MovieService {

    private final ServiceUtil serviceUtil;

    @Autowired
    public BaseMovieService(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }


    @Override
    public Movie getMovie(int movieId) {
        log.debug("/movie return the found movie for movieId={}", movieId);

        //for testing purposes
        if (movieId < 1) throw new InvalidInputException("Invalid productId: " + movieId);
        if (movieId == 13) throw new NotFoundException("No product found for productId: " + movieId);

        return new Movie(movieId, "name-" + movieId, "Horror", serviceUtil.getServiceAddress());
    }
}
