package com.geborskimateusz.microservices.core.movie.persistence;

import com.sun.org.apache.xpath.internal.patterns.StepPattern;
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

        StepVerifier.create(movieRepository.findByMovieId(savedMovieEntity.getMovieId()))
                .expectNextMatches(movieEntity -> areMovieEqual(savedMovieEntity, movieEntity))
                .verifyComplete();
    }

    @Test
    void shouldPerformOptimisticLocking() {
        String concurrentM1actionData = "Concurrent action data performed on M1";
        String concurrentM2actionData = "Concurrent action data performed on M2";

        MovieEntity m1 = movieRepository.findById(savedMovieEntity.getId()).block();
        MovieEntity m2 = movieRepository.findById(savedMovieEntity.getId()).block();

        // by updating Entity its version should be updated
        m1.setTitle(concurrentM1actionData);
        movieRepository.save(m1).block();

        // should fail because of version mismatch
        m2.setTitle(concurrentM2actionData);
        StepVerifier.create(movieRepository.save(m2)).expectError(OptimisticLockingFailureException.class).verify();

        //check current version and state
        StepVerifier.create(movieRepository.findByMovieId(savedMovieEntity.getMovieId()))
                .expectNextMatches(movieEntity ->
                        movieEntity.getVersion() == 1 && concurrentM1actionData.equals(movieEntity.getTitle()))
                .verifyComplete();
    }

    @Test
    void delete() {
        StepVerifier.create(movieRepository.delete(savedMovieEntity)).verifyComplete();
        StepVerifier.create(movieRepository.existsById(savedMovieEntity.getId())).expectNext(false).verifyComplete();
        StepVerifier.create(movieRepository.count()).expectNext(0L).verifyComplete();
    }

    @Test
    void duplicateMovieError() {
        MovieEntity duplicate = MovieEntity.builder().build();
        duplicate.setId(savedMovieEntity.getId());

        assertThrows(DuplicateKeyException.class, () -> movieRepository.save(duplicate));
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