package com.geborskimateusz.microservices.core.movie.persistence;

import com.geborskimateusz.api.core.movie.Movie;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface MovieRepository extends PagingAndSortingRepository<MovieEntity, String> {
    Optional<MovieEntity> findByMovieId(Integer movieId);
}
