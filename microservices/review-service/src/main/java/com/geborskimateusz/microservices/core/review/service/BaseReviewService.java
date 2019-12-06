package com.geborskimateusz.microservices.core.review.service;

import com.geborskimateusz.api.core.review.Review;
import com.geborskimateusz.api.core.review.ReviewService;
import com.geborskimateusz.util.exceptions.InvalidInputException;
import com.geborskimateusz.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
public class BaseReviewService implements ReviewService {

    private final ServiceUtil serviceUtil;

    @Autowired
    public BaseReviewService(ServiceUtil seriviceUtil) {
        this.serviceUtil = seriviceUtil;
    }

    @Override
    public List<Review> getReviews(int movieId) {
        if (movieId < 1) throw new InvalidInputException("Invalid productId: " + movieId);

        if (movieId == 213) {
            log.debug("No reviews found for productId: {}", movieId);
            return  new ArrayList<>();
        }

        List<Review> list = Arrays.asList(
                Review.builder().movieId(movieId).reviewId(1).author("Author 1").subject("Subject 1").content("Content 1").serviceAddress(serviceUtil.getServiceAddress()).build(),
                Review.builder().movieId(movieId).reviewId(2).author("Author 2").subject("Subject 2").content("Content 2").serviceAddress(serviceUtil.getServiceAddress()).build(),
                Review.builder().movieId(movieId).reviewId(3).author("Author 2").subject("Subject 3").content("Content 3").serviceAddress(serviceUtil.getServiceAddress()).build()
        );

        log.debug("/reviews response size: {}", list.size());

        return list;
    }

    @Override
    public Review createReview(Review review) {
        return null;
    }

    @Override
    public void deleteReviews(int movieId) {

    }
}
