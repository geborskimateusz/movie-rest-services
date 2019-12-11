package com.geborskimateusz.microservices.core.recommendation.service;

import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.microservices.core.recommendation.persistence.RecommendationEntity;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecommendationMapperTest {

    public static final String FAKE_CONTENT = "Fake Content";
    public static final int MOVIE_ID = 1;
    public static final int RECOMMENDATION_ID = 2;
    public static final String FAKE_AUTHOR = "Fake Author";
    public static final int RATE = 1;
    public static final String SERVICE_ADDRESS = "123321123";
    public static final int VERSION = 1;

    RecommendationMapper mapper = RecommendationMapper.INSTANCE;

    @Test
    void entityToApi() {
        RecommendationEntity entity = getEntity();

        Recommendation api = mapper.entityToApi(entity);

        assertApi(entity, api);

    }

    private void assertApi(RecommendationEntity entity, Recommendation api) {
        assertEquals(entity.getMovieId(), api.getMovieId());
        assertEquals(entity.getRecommendationId(), api.getRecommendationId());
        assertEquals(entity.getAuthor(), api.getAuthor());
        assertEquals(entity.getRate(), api.getRate());
        assertEquals(entity.getContent(), api.getContent());
        assertNull(api.getServiceAddress());
    }

    @Test
    void apiToEntity() {
        Recommendation api = getApi();

        RecommendationEntity entity = mapper.apiToEntity(api);

        assertEntity(api, entity);
    }

    @Test
    void entityListToApiList() {
        RecommendationEntity entity = getEntity();
        List<RecommendationEntity> recommendationEntities = Collections.singletonList(entity);

        List<Recommendation> recommendations = mapper.entityListToApiList(recommendationEntities);

        Recommendation api = recommendations.get(0);

        assertApi(entity, api);
    }

    @Test
    void apiListToEntityList() {
        Recommendation api = getApi();
        List<Recommendation> recommendations = Collections.singletonList(api);

        List<RecommendationEntity> recommendationEntities = mapper.apiListToEntityList(recommendations);

        RecommendationEntity entity = recommendationEntities.get(0);

        assertEntity(api, entity);
    }

    private void assertEntity(Recommendation api, RecommendationEntity entity) {
        assertEquals(api.getMovieId(), entity.getMovieId());
        assertEquals(api.getRecommendationId(), entity.getRecommendationId());
        assertEquals(api.getAuthor(), entity.getAuthor());
        assertEquals(api.getRate(), entity.getRate());
        assertEquals(api.getContent(), entity.getContent());
        assertNull(entity.getVersion());
        assertNull(entity.getId());
    }

    private Recommendation getApi() {
        return Recommendation.builder()
                .content(FAKE_CONTENT)
                .movieId(MOVIE_ID)
                .recommendationId(RECOMMENDATION_ID)
                .author(FAKE_AUTHOR)
                .rate(RATE)
                .serviceAddress(SERVICE_ADDRESS)
                .build();
    }

    private RecommendationEntity getEntity() {
        return RecommendationEntity.builder()
                .content(FAKE_CONTENT)
                .movieId(MOVIE_ID)
                .recommendationId(RECOMMENDATION_ID)
                .author(FAKE_AUTHOR)
                .rate(RATE)
                .serviceAddress(SERVICE_ADDRESS)
                .version(VERSION)
                .build();
    }
}