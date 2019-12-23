package com.geborskimateusz.microservices.core.movie.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface MovieRepository extends ReactiveCrudRepository<MovieEntity, String> {
    Mono<MovieEntity> findByMovieId(Integer movieId);
}
