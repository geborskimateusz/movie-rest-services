package com.geborskimateusz.microservices.core.review.service;

import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.microservices.core.review.persistence.ReviewEntity;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
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
        Review api = getApi();
        ReviewEntity entity = mapper.apiToEntity(api);

        assertAll(() -> {
            assertEquals(api.getReviewId(), (int) entity.getReviewId());
            assertEquals(api.getMovieId(), (int) entity.getMovieId());
            assertNotNull(api.getServiceAddress());
        });
    }

    @Test
    void entityListToApiList() {
        List<ReviewEntity> reviewEntities = Collections.singletonList(getEntity());
        List<Review> reviews = mapper.entityListToApiList(reviewEntities);

        ReviewEntity entity = reviewEntities.get(0);
        Review api = reviews.get(0);

        assertThat(reviews.size(), equalTo(1));
        assertEquals((int) entity.getId(), api.getReviewId());
        assertEquals((int) entity.getMovieId(), api.getMovieId());
        assertNull(api.getServiceAddress());
    }

    @Test
    void apiListToEntityList() {
        List<Review> reviews = Collections.singletonList(getApi());
        List<ReviewEntity> reviewEntities = mapper.apiListToEntityList(reviews);

        ReviewEntity entity = reviewEntities.get(0);

        assertThat(reviewEntities.size(), equalTo(1));
        assertNull(entity.getId());
        assertNull(entity.getVersion());
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

    private Review getApi() {
        return Review.builder()
                .reviewId(REVIEW_ID)
                .movieId(MOVIE_ID)
                .author(FAKE_AUTHOR)
                .content(FAKE_CONTENT)
                .serviceAddress(FAKE_SERVICE_ADDRESS)
                .subject(FAKE_SUBJECT)
                .build();

    }
}