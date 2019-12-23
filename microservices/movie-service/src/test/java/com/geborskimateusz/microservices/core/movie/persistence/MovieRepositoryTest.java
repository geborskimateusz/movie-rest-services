package com.geborskimateusz.microservices.core.movie.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataMongoTest
public class MovieRepositoryTest {

    public static final int BASE_MOVIE_ID = 1;

    @Autowired
    MovieRepository movieRepository;

    MovieEntity savedMovieEntity;

    @BeforeEach
    void setUp() {
        StepVerifier.create(movieRepository.deleteAll()).verifyComplete();

        MovieEntity movieEntity = MovieEntity
                .builder()
                .movieId(BASE_MOVIE_ID)
                .title("Raise of Jedi")
                .address("123.321.54x24")
                .genre("Sci-Fi")
                .build();

        StepVerifier.create(movieRepository.save(movieEntity))
                .expectNextMatches(entity -> {
                    savedMovieEntity = entity;
                    assertEqualsMovie(movieEntity, savedMovieEntity);
                    return areMovieEqual(movieEntity, savedMovieEntity);
                }).verifyComplete();
    }

    @Test
    void create() {
        MovieEntity movieEntity = MovieEntity
                .builder()
                .movieId(2)
                .title("Fall of Jedi")
                .address("125.721.54x24")
                .genre("Sci-Fi")
                .build();

        StepVerifier.create(movieRepository.save(movieEntity))
                .expectNextMatches(entity -> areMovieEqual(movieEntity, entity))
                .verifyComplete();

        StepVerifier.create(movieRepository.count()).expectNext(2L).verifyComplete();
    }

    @Test
    void update() {
        String givenTitle = "Updated Title";
        savedMovieEntity.setTitle(givenTitle);

        StepVerifier.create(movieRepository.save(savedMovieEntity))
                .expectNextMatches(updatedEntity -> updatedEntity.getTitle().equals(givenTitle))
                .verifyComplete();

        StepVerifier.create(movieRepository.findByMovieId(savedMovieEntity.getMovieId()))
                .expectNextMatches(movieEntity -> movieEntity.getTitle().equals(givenTitle))
                .verifyComplete();
    }


    @Test
    void findById() {
        Optional<MovieEntity> optionalMovieEntity = movieRepository.findById(savedMovieEntity.getId()).blockOptional();

        assertTrue(optionalMovieEntity.isPresent());

        MovieEntity movieEntity = optionalMovieEntity.get();

        assertEqualsMovie(savedMovieEntity, movieEntity);
    }

    @Test
    void shouldPerformOptimisticLocking() {
        String concurrentM1actionData = "Concurrent action data performed on M1";
        String concurrentM2actionData = "Concurrent action data performed on M2";

        MovieEntity m1 = movieRepository.findById(savedMovieEntity.getId()).block();
        MovieEntity m2 = movieRepository.findById(savedMovieEntity.getId()).block();

        m1.setTitle(concurrentM1actionData);

        // by updating Entity its version should be updated
        movieRepository.save(m1);

        // should fail because of version mismatch
        try {
            m2.setTitle(concurrentM2actionData);
            movieRepository.save(m2);
            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {
            System.out.println("shouldPerformOptimisticLocking() -> catch OptimisticLockingFailureException");
        }

        //check current version and state
        MovieEntity updatedMovieEntity = movieRepository.findById(savedMovieEntity.getId()).block();
        assertEquals(1, (int) updatedMovieEntity.getVersion());
        assertEquals(concurrentM1actionData, updatedMovieEntity.getTitle());
    }

    @Test
    void delete() {
        movieRepository.delete(savedMovieEntity);
        assertEquals(0, movieRepository.count());
    }

    @Test
    void duplicateMovieError() {
        MovieEntity duplicate = MovieEntity.builder().build();
        duplicate.setId(savedMovieEntity.getId());

        assertThrows(DuplicateKeyException.class, () -> movieRepository.save(duplicate));
    }

//    @Test
//    void paging() {
//
//        bulkSaveMovie();
//
//        Pageable nextPage = PageRequest.of(0, 4, Sort.Direction.ASC, "movieId");
//
//        nextPage = verifyPages(nextPage, "[1, 2, 3, 4]", true);
//        nextPage = verifyPages(nextPage, "[5, 6, 7, 8]", true);
//        verifyPages(nextPage, "[9, 10]", false);
//
//    }
//
//    private Pageable verifyPages(Pageable nextPage, String idsAsString, boolean hasNext) {
//        Page<MovieEntity> moviePage = movieRepository.findAll(nextPage);
//        assertEquals(hasNext, moviePage.hasNext());
//
//        String ids = moviePage.get().map(MovieEntity::getMovieId).collect(Collectors.toList()).toString();
//        assertEquals(idsAsString, ids);
//        return nextPage.next();
//    }

    private void bulkSaveMovie() {
        movieRepository.deleteAll();

        List<MovieEntity> movies = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> MovieEntity.builder()
                        .movieId(i)
                        .title("Movie nr: " + i)
                        .build())
                .collect(Collectors.toList());

        movieRepository.saveAll(movies);
    }


    private void assertEqualsMovie(MovieEntity expected, MovieEntity actual) {
        assertAll("Executing assertEqualsMovie(..)", () -> {
            assertEquals(expected.getId(), actual.getId());
            assertEquals(expected.getVersion(), actual.getVersion());
            assertEquals(expected.getMovieId(), actual.getMovieId());
            assertEquals(expected.getTitle(), actual.getTitle());
            assertEquals(expected.getAddress(), actual.getAddress());
            assertEquals(expected.getGenre(), actual.getGenre());
        });
    }

    private boolean areMovieEqual(MovieEntity expected, MovieEntity actual) {
        return (expected.getId().equals(actual.getId())) &&
                (expected.getVersion().equals(actual.getVersion())) &&
                (expected.getMovieId().equals(actual.getMovieId())) &&
                (expected.getTitle().equals(actual.getTitle())) &&
                (expected.getAddress().equals(actual.getAddress())) &&
                (expected.getGenre().equals(actual.getGenre()));

    }
}