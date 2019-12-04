package com.geborskimateusz.api.core.movie;

import org.springframework.web.bind.annotation.*;

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

    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/movie \
     *   -H "Content-Type: application/json" --data \
     *   '{"movieId":123,"name":"movie 123", ...}'
     *
     * @param movie
     * @return
     */
    Movie createMovie(@RequestBody Movie movie);

    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/movie/1
     *
     * @param movieId
     */
    @DeleteMapping(value = "/movie/${movieId}")
    void deleteMovie(@PathVariable  Integer movieId);
}
