package com.geborskimateusz.microservices.core.review.service;

import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.api.core.review.ReviewService;
import com.geborskimateusz.microservices.core.review.persistence.ReviewEntity;
import com.geborskimateusz.microservices.core.review.persistence.ReviewRepository;
import com.geborskimateusz.util.exceptions.InvalidInputException;
import com.geborskimateusz.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
public class BaseReviewService implements ReviewService {

    private final ServiceUtil serviceUtil;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper mapper = ReviewMapper.INSTANCE;

    @Autowired
    public BaseReviewService(ServiceUtil seriviceUtil, ReviewRepository reviewRepository) {
        this.serviceUtil = seriviceUtil;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public List<Review> getReviews(int movieId) {
        if (movieId < 1) throw new InvalidInputException("Invalid productId: " + movieId);

        List<ReviewEntity> reviewEntities = reviewRepository.findByMovieId(movieId);
        List<Review> reviews = mapper.entityListToApiList(reviewEntities);
        reviews.forEach(review -> review.setServiceAddress(serviceUtil.getServiceAddress()));

        log.debug("getReviews: response size: {}", reviews.size());

        return reviews;
    }

    @Override
    public Review createReview(Review review) {
        try {
            ReviewEntity reviewEntity = mapper.apiToEntity(review);
            ReviewEntity saved = reviewRepository.save(reviewEntity);

            log.debug("createReview: created a review entity: {}/{}", saved.getMovieId(), saved.getReviewId());

            return mapper.entityToApi(saved);

        } catch (DataIntegrityViolationException e) {
            throw new InvalidInputException("Duplicate key, Movie Id: " + review.getMovieId() + ", Review Id:" + review.getReviewId());
        }
    }

    @Override
    public void deleteReviews(int movieId) {
        log.debug("deleteReviews: tries to delete reviews for the movie with movieId: {}", movieId);
        reviewRepository.deleteAll(reviewRepository.findByMovieId(movieId));
    }
}
