package com.geborskimateusz.api.core.recommendation;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface RecommendationService {

    /**
     * Sample usage: curl $HOST:$PORT/recommendation?movieId=1
     *
     * @param movieId
     * @return
     */
    @GetMapping(
            value    = "/recommendation",
            produces = "application/json")
    List<Recommendation> getRecommendations(@RequestParam(value = "movieId", required = true) int movieId);

    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/recommendation \
     *   -H "Content-Type: application/json" --data \
     *   '{"movieId":123,"recommendationId":456,"author":"me", ...}'
     *
     * @param recommendation
     * @return
     */
    @PostMapping(
            value    = "/recommendation",
            consumes = "application/json",
            produces = "application/json")
    Recommendation createRecommendation(@RequestBody Recommendation recommendation);

    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/recommendation?movieId=1
     *
     * @param movieId
     */
    @DeleteMapping(value = "/recommendation")
    void deleteRecommendations(@RequestParam(value = "movieId", required = true)  int movieId);
}
