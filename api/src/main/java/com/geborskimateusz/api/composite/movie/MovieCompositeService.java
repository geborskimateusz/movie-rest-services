package com.geborskimateusz.api.composite.movie;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;

@Api(description = "Rest API for composite movie information.")
public interface MovieCompositeService {


    /**
     * Sample usage: curl $HOST:$PORT/movie-composite/1
     *
     * @param movieId
     * @return the composite movie info, if found, else null
     */
    @ApiOperation(
            value = "${api.movie-composite.get-composite-movie.description}",
            notes = "${api.movie-composite.get-composite-movie.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 404, message = "Not found, the specified id does not exist."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fails. See response message for more information.")
    })
    @GetMapping(
            value = "/movie-composite/{movieId}",
            produces = "application/json")
    MovieAggregate getCompositeMovie(@PathVariable int movieId);


    /**
     * Sample usage: curl $HOST:$PORT/movie-composite
     *
     * @param body
     */
    @ApiOperation(
            value = "${api.movie-composite.create-composite-movie.description}",
            notes = "${api.movie-composite.create-composite-movie.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fails. See response message for more information.")
    })
    @PostMapping(
            value = "/movie-composite",
            produces = "application/json")
    void createCompositeMovie(@RequestBody MovieAggregate body);


    /**
     * Sample usage: curl $HOST:$PORT/movie-composite/1
     *
     * @param movieId
     */
    @ApiOperation(
            value = "${api.movie-composite.delete-composite-movie.description}",
            notes = "${api.movie-composite.delete-composite-movie.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fails. See response message for more information.")
    })
    @DeleteMapping(
            value = "/movie-composite/{movieId}",
            produces = "application/json")
    void deleteCompositeMovie(@PathVariable int movieId);
}
