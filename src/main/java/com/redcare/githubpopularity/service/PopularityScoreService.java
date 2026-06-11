package com.redcare.githubpopularity.service;

import com.redcare.githubpopularity.config.PopularityWeightsProperties;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

@Service
public class PopularityScoreService {

    private final PopularityWeightsProperties popularityWeightsProperties;

    public PopularityScoreService(final PopularityWeightsProperties popularityWeightsProperties) {
        this.popularityWeightsProperties = popularityWeightsProperties;
    }

    public double calculateScore(final int stars, final int forks, final Instant updatedAt) {
        double recencyScore = calculateRecencyScore(updatedAt);
        return (stars * popularityWeightsProperties.stars())
              + (forks * popularityWeightsProperties.forks())
              + (recencyScore * popularityWeightsProperties.recency());
    }

    private double calculateRecencyScore(final Instant updatedAt) {
        if (updatedAt == null) {
            return 0;
        }

        long daysSinceUpdate = ChronoUnit.DAYS.between(updatedAt, Instant.now());

        if (daysSinceUpdate <= 30) {
            return 100;
        }

        if (daysSinceUpdate <= 90) {
            return 75;
        }

        if (daysSinceUpdate <= 180) {
            return 50;
        }

        if (daysSinceUpdate <= 365) {
            return 25;
        }

        return 0;
    }
}
