package com.geborskimateusz.microservices.core.movie.service;

import com.geborskimateusz.api.core.movie.Movie;
import com.geborskimateusz.microservices.core.movie.persistence.MovieEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Movie entityToApi(MovieEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    MovieEntity apiToEntity(Movie api);
}
