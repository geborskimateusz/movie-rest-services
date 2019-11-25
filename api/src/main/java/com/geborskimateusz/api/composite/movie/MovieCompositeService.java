package com.geborskimateusz.api.composite.movie;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface MovieCompositeService {
    /**
     * Sample usage: curl $HOST:$PORT/movie-composite/1
     *
     * @param movieId
     * @return the composite movie info, if found, else null
     */
    @GetMapping(
            value    = "/movie-composite/{movieId}",
            produces = "application/json")
    MovieAggregate getMovie(@PathVariable int movieId);
}
