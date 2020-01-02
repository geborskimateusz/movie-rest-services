package com.geborskimateusz.microservices.core.recommendation.service;

import com.geborskimateusz.api.core.recommendation.Recommendation;
import com.geborskimateusz.api.core.recommendation.RecommendationService;
import com.geborskimateusz.microservices.core.recommendation.persistence.RecommendationEntity;
import com.geborskimateusz.microservices.core.recommendation.persistence.RecommendationRepository;
import com.geborskimateusz.util.exceptions.InvalidInputException;
import com.geborskimateusz.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
public class BaseRecommendationService implements RecommendationService {

    private final ServiceUtil serviceUtil;
    private  RecommendationRepository recommendationRepository;

    private final RecommendationMapper mapper = RecommendationMapper.INSTANCE;

    public BaseRecommendationService(ServiceUtil serviceUtil, RecommendationRepository recommendationRepository) {
        this.serviceUtil = serviceUtil;
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    public Flux<Recommendation> getRecommendations(int movieId) {

        if (movieId < 1) throw new InvalidInputException("Invalid movieId: " + movieId);

        return recommendationRepository.findByMovieId(movieId)
                .log()
                .map(mapper::entityToApi)
                .map(e -> {e.setServiceAddress(serviceUtil.getServiceAddress()); return e;});
    }

    @Override
    public Recommendation createRecommendation(Recommendation recommendation) {
        log.debug("createRecommendation: Trying to create recommendation entity: {}/{}", recommendation.getMovieId(), recommendation.getRecommendationId());

        try {
            RecommendationEntity recommendationEntity = mapper.apiToEntity(recommendation);
            RecommendationEntity saved = recommendationRepository.save(recommendationEntity).block();

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
