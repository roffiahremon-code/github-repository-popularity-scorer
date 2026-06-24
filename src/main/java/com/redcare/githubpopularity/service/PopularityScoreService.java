package com.redcare.githubpopularity.service;

import com.redcare.githubpopularity.config.PopularityWeightsProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopularityScoreService {

    private final PopularityWeightsProperties popularityWeightsProperties;
    private final Clock clock;

    public double calculateScore(final int stars, final int forks, final Instant updatedAt) {
        final double recencyScore = calculateRecencyScore(updatedAt);
        final double score = (stars * popularityWeightsProperties.stars())
              + (forks * popularityWeightsProperties.forks())
              + (recencyScore * popularityWeightsProperties.recency());
        return Math.round(score * 100.0) / 100.0;
    }

    private double calculateRecencyScore(final Instant updatedAt) {
        if (updatedAt == null) {
            return 0;
        }

        final long daysSinceUpdate = ChronoUnit.DAYS.between(updatedAt, Instant.now(clock));
        final double recencyMaxDays = popularityWeightsProperties.recencyMaxDays();

        return Math.clamp(100 * (1 - daysSinceUpdate / recencyMaxDays), 0, 100);
    }
}
