package com.geborskimateusz.microservices.core.review.persistence;

import com.geborskimateusz.api.core.review.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.apache.logging.log4j.ThreadContext.isEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
class ReviewRepositoryTest {

    public static final int BASE_MOVIE_ID = 1;
    public static final int BASE_REVIEW_ID = 2;
    @Autowired
    ReviewRepository reviewRepository;

    ReviewEntity savedReviewEntity;

    @Autowired
    EntityManager entityManager;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();

        ReviewEntity reviewEntity = ReviewEntity.builder()
                .movieId(BASE_MOVIE_ID)
                .reviewId(BASE_REVIEW_ID)
                .author("Fake Author")
                .content("Fake Content")
                .subject("Fake Subject")
                .serviceAddress("Fake Service Address")
                .build();

        savedReviewEntity = reviewRepository.save(reviewEntity);

        assertReview(reviewEntity, savedReviewEntity);
    }

    @Test
    void create() {
        reviewRepository.deleteAll();

        ReviewEntity reviewEntity = ReviewEntity.builder()
                .movieId(BASE_MOVIE_ID)
                .reviewId(BASE_REVIEW_ID)
                .author("Fake Author")
                .content("Fake Content")
                .subject("Fake Subject")
                .serviceAddress("Fake Service Address")
                .build();

        ReviewEntity saved = reviewRepository.save(reviewEntity);

        assertReview(reviewEntity, saved);
    }

    @Test
    void update() {

        String updatedContent = "Updated Content";

        savedReviewEntity.setContent(updatedContent);

        reviewRepository.save(savedReviewEntity);

        ReviewEntity updated = reviewRepository.findById(savedReviewEntity.getId()).get();

        assertEquals(updatedContent, updated.getContent());

    }


    @Test
    void delete() {

        reviewRepository.delete(savedReviewEntity);

        assertEquals(0, reviewRepository.count());

    }

    @Test
    void findByMovieId() {

        List<ReviewEntity> reviewEntities = reviewRepository.findByMovieId(savedReviewEntity.getMovieId());
        ReviewEntity firstEntity = reviewEntities.get(0);

        assertThat(reviewEntities, hasSize(1));
        assertReview(savedReviewEntity, firstEntity);

    }


    //TODO, posted on StackOverflow
    @Disabled
    @Test
    void onDuplicate() {

        ReviewEntity duplicated = savedReviewEntity;

        assertEquals(savedReviewEntity.getId(), duplicated.getId());

        assertThrows(DataIntegrityViolationException.class, () -> {
            reviewRepository.save(duplicated);
        });
    }


    @Test
    void optimisticLockVerification() {
        String r1ConcurrentContent = "r1ConcurrentContent";
        String r2ConcurrentContent = "r2ConcurrentContent";

        ReviewEntity r1 = reviewRepository.findById(savedReviewEntity.getId()).get();
        ReviewEntity r2 = reviewRepository.findById(savedReviewEntity.getId()).get();

        r1.setContent(r1ConcurrentContent);
        reviewRepository.save(r1);

        try {
            r2.setContent(r2ConcurrentContent);
            reviewRepository.save(r2);

            fail("Expected an OptimisticLockingFailureException");
        } catch (OptimisticLockingFailureException e) {
            System.out.println("OptimisticLockingFailureException should be throw.");
        }

        ReviewEntity updated = reviewRepository.findById(savedReviewEntity.getId()).get();
        assertEquals(1, (int) updated.getVersion());
        assertEquals(r1ConcurrentContent, updated.getContent());
    }

    private void assertReview(ReviewEntity expected, ReviewEntity actual) {
        assertAll("Executing assertReview(..)", () -> {
            assertEquals(expected.getId(), actual.getId());
            assertEquals(expected.getVersion(), actual.getVersion());
            assertEquals(expected.getMovieId(), actual.getMovieId());
            assertEquals(expected.getReviewId(), actual.getReviewId());
            assertEquals(expected.getContent(), actual.getContent());
            assertEquals(expected.getAuthor(), actual.getAuthor());
            assertEquals(expected.getSubject(), actual.getSubject());
            assertEquals(expected.getServiceAddress(), actual.getServiceAddress());
        });
    }
}