package com.geborskimateusz.microservices.core.recommendation.service;

import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.recommendation.RecommendationService;
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
public class BaseRecommendationService implements RecommendationService {

    private final ServiceUtil serviceUtil;

    @Autowired
    public BaseRecommendationService(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Recommendation> getRecommendations(int movieId) {

        //for testing purposes
        if (movieId < 1) throw new InvalidInputException("Invalid productId: " + movieId);

        if (movieId == 113) {
            log.debug("No recommendations found for productId: {}", movieId);
            return  new ArrayList<>();
        }



        List<Recommendation> list = Arrays.asList(
                Recommendation.builder().movieId(movieId).recommendationId(1).author("Author 1").rate(1).content("Content 1").serviceAddress(serviceUtil.getServiceAddress()).build(),
                Recommendation.builder().movieId(movieId).recommendationId(2).author("Author 2").rate(2).content("Content 2").serviceAddress(serviceUtil.getServiceAddress()).build(),
                Recommendation.builder().movieId(movieId).recommendationId(3).author("Author 3").rate(3).content("Content 3").serviceAddress(serviceUtil.getServiceAddress()).build()
        );

        log.debug("/recommendation response size: {}", list.size());

        return list;
    }

    @Override
    public Recommendation createRecommendation(Recommendation recommendation) {
        return null;
    }

    @Override
    public void deleteRecommendations(int movieId) {

    }
}
