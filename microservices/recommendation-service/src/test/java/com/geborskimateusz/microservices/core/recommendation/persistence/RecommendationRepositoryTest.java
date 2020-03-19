package com.geborskimateusz.microservices.core.recommendation.persistence;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(SpringExtension.class)
@DataMongoTest(properties = "spring.cloud.config.enabled=false")
public class RecommendationRepositoryTest {

    public static final int BASE_RECOMMENDATION_ID = 1;
    public static final int BASE_MOVIE_ID = 2;

    @Autowired
    RecommendationRepository recommendationRepository;

    RecommendationEntity savedRecommendationEntity;


    @BeforeEach
    void setUp() {
        recommendationRepository.deleteAll().block();

        RecommendationEntity given = RecommendationEntity.builder()
                .recommendationId(BASE_RECOMMENDATION_ID)
                .movieId(BASE_MOVIE_ID)
                .content("Fake recommendation")
                .build();

        StepVerifier.create(recommendationRepository.save(given))
                .expectNextMatches(recommendationEntity -> {
                    savedRecommendationEntity = recommendationEntity;
                    return assertRecommendation(given, recommendationEntity);
                })
                .verifyComplete();
    }

    @Test
    void create() {
        recommendationRepository.deleteAll().block();

        RecommendationEntity given = RecommendationEntity.builder()
                .recommendationId(BASE_RECOMMENDATION_ID)
                .movieId(BASE_MOVIE_ID)
                .content("Fake recommendation")
                .build();

        StepVerifier.create(recommendationRepository.save(given))
                .expectNextMatches(recommendationEntity -> assertRecommendation(given, recommendationEntity))
                .verifyComplete();

    }

    @Test
    void findByMovieId() {
        List<RecommendationEntity> recommendationEntities =
                recommendationRepository.findByMovieId(savedRecommendationEntity.getMovieId())
                        .collectList().block();

        assertThat(recommendationEntities, hasSize(1));
        assertRecommendation(savedRecommendationEntity, recommendationEntities.get(0));
    }

    @Test
    void update() {
        String newContent = "Updated Content";
        savedRecommendationEntity.setContent(newContent);

        StepVerifier.create(recommendationRepository.save(savedRecommendationEntity))
                .expectNextMatches(recommendationEntity ->
                        recommendationEntity.getId().equals(savedRecommendationEntity.getId()) &&
                                recommendationEntity.getContent().equals(newContent))
                .verifyComplete();
    }

    @Test
    void delete() {
        recommendationRepository.delete(savedRecommendationEntity).block();
        assertThat(recommendationRepository.count().block(), equalTo(0L));

    }

    @Test
    void duplicateError() {

        RecommendationEntity duplicate = RecommendationEntity.builder().build();
        duplicate.setId(savedRecommendationEntity.getId());

        StepVerifier.create(recommendationRepository.save(duplicate)).expectError(DuplicateKeyException.class).verify();
    }

    @Test
    void optimisticLockError() {
        String r1ConcurrentContent = "r1ConcurrentContent";
        String r2ConcurrentContent = "r2ConcurrentContent";

        RecommendationEntity r1 = recommendationRepository.findById(savedRecommendationEntity.getId()).block();
        RecommendationEntity r2 = recommendationRepository.findById(savedRecommendationEntity.getId()).block();

        r1.setContent(r1ConcurrentContent);
        recommendationRepository.save(r1).block();

        r2.setContent(r2ConcurrentContent);
        StepVerifier.create(recommendationRepository.save(r2)).expectError(OptimisticLockingFailureException.class).verify();

        StepVerifier.create(recommendationRepository.findById(savedRecommendationEntity.getId()))
                .expectNextMatches(updated ->
                        updated.getVersion() == 1 &&
                                r1ConcurrentContent.equals(updated.getContent()));
    }

    private boolean assertRecommendation(RecommendationEntity expected, RecommendationEntity actual) {
        return expected.getId() == actual.getId() &&
                expected.getVersion() == actual.getVersion() &&
                expected.getRecommendationId() == actual.getRecommendationId() &&
                expected.getMovieId() == actual.getMovieId() &&
                expected.getServiceAddress() == actual.getServiceAddress() &&
                expected.getAuthor() == actual.getAuthor() &&
                expected.getContent() == actual.getContent();
    }
}