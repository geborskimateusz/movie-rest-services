package com.geborskimateusz.microservices.core.movie.persistence;

import com.geborskimateusz.microservices.core.movie.service.BaseMovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

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
        movieRepository.deleteAll();

        MovieEntity movieEntity = MovieEntity
                .builder()
                .movieId(BASE_MOVIE_ID)
                .title("Raise of Jedi")
                .address("123.321.54x24")
                .genre("Sci-Fi")
                .build();

        savedMovieEntity = movieRepository.save(movieEntity);

        assertEqualsMovie(movieEntity, savedMovieEntity);
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

        MovieEntity saved = movieRepository.save(movieEntity);

        assertEqualsMovie(movieEntity, saved);
        assertEquals(2, movieRepository.count());
    }

    @Test
    void update() {
        String givenTitle = "Updated Title";

        savedMovieEntity.setTitle(givenTitle);

        MovieEntity updated = movieRepository.save(savedMovieEntity);

        assertEquals(givenTitle, updated.getTitle());
    }

    @Test
    void findById() {
        Optional<MovieEntity> optionalMovieEntity = movieRepository.findById(savedMovieEntity.getId());

        assertTrue(optionalMovieEntity.isPresent());

        MovieEntity movieEntity = optionalMovieEntity.get();

        assertEqualsMovie(savedMovieEntity, movieEntity);
    }

    @Test
    void shouldPerformOptimisticLocking() {
        String concurrentM1actionData = "Concurrent action data performed on M1";
        String concurrentM2actionData = "Concurrent action data performed on M2";

        MovieEntity m1 = movieRepository.findById(savedMovieEntity.getId()).get();
        MovieEntity m2 = movieRepository.findById(savedMovieEntity.getId()).get();

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
        MovieEntity updatedMovieEntity = movieRepository.findById(savedMovieEntity.getId()).get();
        assertEquals(1, (int) updatedMovieEntity.getVersion());
        assertEquals(concurrentM1actionData, updatedMovieEntity.getTitle());

    }


    private void assertEqualsMovie(MovieEntity expected, MovieEntity actual) {
        assertAll("Executing assertEqualsMovie.", () -> {
            assertEquals(expected.getId(), actual.getId());
            assertEquals(expected.getVersion(), actual.getVersion());
            assertEquals(expected.getMovieId(), actual.getMovieId());
            assertEquals(expected.getTitle(), actual.getTitle());
            assertEquals(expected.getAddress(), actual.getAddress());
            assertEquals(expected.getGenre(), actual.getGenre());
        });
    }
}