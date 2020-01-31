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
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Iterator;
import java.util.List;

@Slf4j
@RestController
public class BaseReviewService implements ReviewService {

    private final ServiceUtil serviceUtil;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper mapper = ReviewMapper.INSTANCE;
    private final Scheduler scheduler;

    @Autowired
    public BaseReviewService(ServiceUtil seriviceUtil, ReviewRepository reviewRepository, Scheduler scheduler) {
        this.serviceUtil = seriviceUtil;
        this.reviewRepository = reviewRepository;
        this.scheduler = scheduler;
    }

    @Override
    public Flux<Review> getReviews(int movieId) {
        if (movieId < 1) throw new InvalidInputException("Invalid productId: " + movieId);

        List<Review> reviews = getByMovieId(movieId);

        return getAsyncFlux(reviews).log();
    }

    @Override
    public Review createReview(Review review) {
        log.info("createReview: Trying to create Review entity, passed argument: {}",review.toString());

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

    private <T> Flux<T> getAsyncFlux(Iterable<T> reviews) {
        return Flux.fromIterable(reviews).publishOn(scheduler);
    }

    private List<Review> getByMovieId(int movieId) {
        List<ReviewEntity> reviewEntities = reviewRepository.findByMovieId(movieId);
        List<Review> reviews = mapper.entityListToApiList(reviewEntities);
        reviews.forEach(review -> review.setServiceAddress(serviceUtil.getServiceAddress()));
        return reviews;
    }
}
