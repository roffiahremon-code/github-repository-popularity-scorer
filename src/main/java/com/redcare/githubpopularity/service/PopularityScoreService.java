package com.redcare.githubpopularity.service;

import com.redcare.githubpopularity.config.PopularityWeightsProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

@Service
public class PopularityScoreService {

    private final PopularityWeightsProperties popularityWeightsProperties;
    private final Clock clock;

    public PopularityScoreService(final PopularityWeightsProperties popularityWeightsProperties, final Clock clock) {
        this.popularityWeightsProperties = popularityWeightsProperties;
        this.clock = clock;
    }

    public double calculateScore(final int stars, final int forks, final Instant updatedAt) {
        double recencyScore = calculateRecencyScore(updatedAt);
        double score = (stars * popularityWeightsProperties.stars())
              + (forks * popularityWeightsProperties.forks())
              + (recencyScore * popularityWeightsProperties.recency());
        return Math.round(score * 100.0) / 100.0;
    }

    private double calculateRecencyScore(final Instant updatedAt) {
        if (updatedAt == null) {
            return 0;
        }

        long daysSinceUpdate = ChronoUnit.DAYS.between(updatedAt, Instant.now(clock));
        return Math.clamp(100 * (1 - daysSinceUpdate / popularityWeightsProperties.recencyMaxDays()), 0, 100);
    }
}
