package com.geborskimateusz.microservices.core.recommendation.service;

import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.recommendation.RecommendationService;
import com.geborskimateusz.microservices.core.recommendation.persistence.RecommendationEntity;
import com.geborskimateusz.microservices.core.recommendation.persistence.RecommendationRepository;
import com.geborskimateusz.util.exceptions.InvalidInputException;
import com.geborskimateusz.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
public class BaseRecommendationService implements RecommendationService {

    private final ServiceUtil serviceUtil;
    private final RecommendationRepository recommendationRepository;

    private final RecommendationMapper mapper = RecommendationMapper.INSTANCE;

    @Autowired
    public BaseRecommendationService(ServiceUtil serviceUtil, RecommendationRepository recommendationRepository) {
        this.serviceUtil = serviceUtil;
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    public List<Recommendation> getRecommendations(int movieId) {

        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);

        List<RecommendationEntity> recommendationEntities = recommendationRepository.findByMovieId(movieId);
        List<Recommendation> recommendations = mapper.entityListToApiList(recommendationEntities);
        recommendations.forEach(recommendation -> recommendation.setServiceAddress(serviceUtil.getServiceAddress()));

        log.debug("Recommendation response size: {}", recommendations.size());

        return recommendations;
    }

    @Override
    public Recommendation createRecommendation(Recommendation recommendation) {
        log.debug("createRecommendation: Trying to create recommendation entity: {}/{}", recommendation.getMovieId(), recommendation.getRecommendationId());

        try {
            RecommendationEntity recommendationEntity = mapper.apiToEntity(recommendation);
            RecommendationEntity saved = recommendationRepository.save(recommendationEntity);

            log.debug("createRecommendation: created a recommendation entity: {}/{}", recommendation.getMovieId(), recommendation.getRecommendationId());

            return mapper.entityToApi(saved);
        }catch (DuplicateKeyException e) {
            throw new DuplicateKeyException("Non unique id for recommendation " + recommendation.getRecommendationId());
        }

    }

    @Override
    public void deleteRecommendations(int movieId) {
        log.debug("deleteRecommendations: Trying to delete recommendation entity for movie " + movieId);

        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);
        recommendationRepository.deleteAll(recommendationRepository.findByMovieId(movieId));
    }
}
