package com.geborskimateusz.api.core.review;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ReviewService {

    /**
     * Sample usage: curl $HOST:$PORT/review?movieId=1
     *
     * @param movieId
     * @return
     */
    @GetMapping(
        value    = "/review",
        produces = "application/json")
    List<Review> getReviews(@RequestParam(value = "movieId", required = true) int movieId);

    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/review \
     *   -H "Content-Type: application/json" --data \
     *   '{"movieId":123,"reviewId":456,"author":"me", ...}'
     *
     * @param review
     * @return
     */
    @PostMapping(
            value    = "/review",
            consumes = "application/json",
            produces = "application/json")
    Review createReview(@RequestBody Review review);

    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/review?movieId=1
     *
     * @param movieId
     */
    @DeleteMapping(value = "/review")
    void deleteReviews(@RequestParam(value = "movieId", required = true)  int movieId);
}