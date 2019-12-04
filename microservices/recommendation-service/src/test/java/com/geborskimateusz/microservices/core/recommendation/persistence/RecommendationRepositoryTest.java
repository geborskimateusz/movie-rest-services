package com.geborskimateusz.microservices.core.recommendation.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataMongoTest
public class RecommendationRepositoryTest {

    public static final int BASE_RECOMMENDATION_ID = 1;
    public static final int BASE_MOVIE_ID = 2;

    @Autowired
    private RecommendationRepository recommendationRepository;

    RecommendationEntity savedRecommendationEntity;


    @BeforeEach
    void setUp() {
        recommendationRepository.deleteAll();

        RecommendationEntity recommendationEntity = RecommendationEntity.builder()
                .recommendationId(BASE_RECOMMENDATION_ID)
                .movieId(BASE_MOVIE_ID)
                .content("Fake recommendation")
                .build();

        savedRecommendationEntity = recommendationRepository.save(recommendationEntity);
        assertRecommendation(recommendationEntity, savedRecommendationEntity);
    }

    @Test
    void create() {
        recommendationRepository.deleteAll();

        RecommendationEntity recommendationEntity = RecommendationEntity.builder()
                .recommendationId(BASE_RECOMMENDATION_ID)
                .movieId(BASE_MOVIE_ID)
                .content("Fake recommendation")
                .build();

        RecommendationEntity saved = recommendationRepository.save(recommendationEntity);
        assertRecommendation(recommendationEntity, saved);
    }

    @Test
    void findByMovieId() {
        List<RecommendationEntity> recommendationEntities =
                recommendationRepository.findByMovieId(savedRecommendationEntity.getMovieId());

        assertThat(recommendationEntities, hasSize(1));
        assertRecommendation(savedRecommendationEntity, recommendationEntities.get(0));
    }

    @Test
    void update() {
        String newContent = "Updated Content";
        savedRecommendationEntity.setContent(newContent);

        RecommendationEntity updated = recommendationRepository.save(savedRecommendationEntity);
        assertEquals(savedRecommendationEntity.getId(), updated.getId());
        assertEquals(newContent, updated.getContent());
    }

    @Test
    void delete() {
        recommendationRepository.delete(savedRecommendationEntity);
        assertEquals(0, recommendationRepository.count());
    }

    @Test
    void duplicateError() {

        RecommendationEntity duplicate = RecommendationEntity.builder().build();
        duplicate.setId(savedRecommendationEntity.getId());

        assertThrows(DuplicateKeyException.class, () -> {
            recommendationRepository.save(duplicate);
        });


    }

    @Test
    void optimisticLockError() {
        String r1ConcurrentContent = "r1ConcurrentContent";
        String r2ConcurrentContent = "r2ConcurrentContent";

        RecommendationEntity r1 = recommendationRepository.findById(savedRecommendationEntity.getId()).get();
        RecommendationEntity r2 = recommendationRepository.findById(savedRecommendationEntity.getId()).get();

        r1.setContent(r1ConcurrentContent);
        recommendationRepository.save(r1);

        try {
            r2.setContent(r2ConcurrentContent);
            recommendationRepository.save(r2);

            fail("Expected an OptimisticLockingFailureException");
        }catch (OptimisticLockingFailureException e) {
            System.out.println("OptimisticLockingFailureException should be throw.");
        }

        RecommendationEntity updated = recommendationRepository.findById(savedRecommendationEntity.getId()).get();
        assertEquals(1, (int) updated.getVersion());
        assertEquals(r1ConcurrentContent, updated.getContent());
    }

    private void assertRecommendation(RecommendationEntity expected, RecommendationEntity actual) {
        assertAll("Executing assertRecommendation(..)", () -> {
            assertEquals(expected.getId(),actual.getId());
            assertEquals(expected.getVersion(),actual.getVersion());
            assertEquals(expected.getRecommendationId(),actual.getRecommendationId());
            assertEquals(expected.getMovieId(),actual.getMovieId());
            assertEquals(expected.getServiceAddress(),actual.getServiceAddress());
            assertEquals(expected.getAuthor(),actual.getAuthor());
            assertEquals(expected.getContent(),actual.getContent());
        });
    }
}