package com.geborskimateusz.api.core.movie;

import io.swagger.models.auth.In;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface MovieService {

    /**
     * Sample usage: curl $HOST:$PORT/movie/1
     *
     * @param movieId
     * @return the movie, if found, else null
     */
    @GetMapping(
            value = "/movie/{movieId}",
            produces = "application/json")
    Mono<Movie> getMovie(@PathVariable Integer movieId);

    /**
     * Sample usage:
     * <p>
     * curl -X POST $HOST:$PORT/movie \
     * -H "Content-Type: application/json" --data \
     * '{"movieId":123,"name":"movie 123", ...}'
     *
     * @param movie
     * @return
     */
    @PostMapping(
            value = "/movie",
            consumes = "application/json",
            produces = "application/json")
    Movie createMovie(@RequestBody Movie movie);

    /**
     * Sample usage:
     * <p>
     * curl -X DELETE $HOST:$PORT/movie/1
     *
     * @param movieId
     */
    @DeleteMapping(value = "/movie/{movieId}")
    void deleteMovie(@PathVariable Integer movieId);
}
