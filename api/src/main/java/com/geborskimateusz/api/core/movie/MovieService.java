package com.geborskimateusz.api.core.movie;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface MovieService {

    /**
     * Sample usage: curl $HOST:$PORT/movie/1
     *
     * @param movieId
     * @return the movie, if found, else null
     */
    @GetMapping(
            value    = "/movie/{movieId}",
            produces = "application/json")
    Movie getMovie(@PathVariable int movieId);
}
