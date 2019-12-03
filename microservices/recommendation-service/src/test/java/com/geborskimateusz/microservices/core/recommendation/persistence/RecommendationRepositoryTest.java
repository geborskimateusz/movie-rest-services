package com.geborskimateusz.microservices.core.recommendation.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
    void delete() {}

    @Test
    void duplicateError() {}

    @Test
    void optimisticLockError() {}

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