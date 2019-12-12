package com.geborskimateusz.microservices.core.review.service;

import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.microservices.core.review.persistence.ReviewEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviewMapperTest {

    public static final String FAKE_SUBJECT = "Fake Subject";
    public static final String FAKE_SERVICE_ADDRESS = "Fake Service Address";
    public static final String FAKE_CONTENT = "Fake Content";
    public static final String FAKE_AUTHOR = "Fake Author";
    public static final int VERSION = 1234;
    public static final int MOVIE_ID = 2;
    public static final int REVIEW_ID = 1;

    ReviewMapper mapper = ReviewMapper.INSTANCE;

    @Test
    void entityToApi() {
        ReviewEntity entity = getEntity();
        Review api = mapper.entityToApi(entity);

        assertAll(() -> {
            assertEquals((int) entity.getId(), api.getReviewId());
            assertEquals((int) entity.getMovieId(), api.getMovieId());
            assertNull(api.getServiceAddress());
        });
    }

    @Test
    void apiToEntity() {
        
    }

    @Test
    void entityListToApiList() {
    }

    @Test
    void apiListToEntityList() {
    }

    private ReviewEntity getEntity() {
        return ReviewEntity.builder()
                .id(1)
                .reviewId(REVIEW_ID)
                .movieId(MOVIE_ID)
                .version(VERSION)
                .author(FAKE_AUTHOR)
                .content(FAKE_CONTENT)
                .serviceAddress(FAKE_SERVICE_ADDRESS)
                .subject(FAKE_SUBJECT)
                .build();
    }
}