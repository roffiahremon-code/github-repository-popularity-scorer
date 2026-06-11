package com.redcare.githubpopularity.service;

import com.redcare.githubpopularity.config.PopularityWeightsProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PopularityScoreServiceTest {

    private static final double STARS_WEIGHT = 0.6;
    private static final double FORKS_WEIGHT = 0.3;
    private static final double RECENCY_WEIGHT = 0.1;
    private static final double RECENCY_MAX_DAYS = 365.0;

    private static final Instant NOW = Instant.parse("2026-06-11T12:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private PopularityScoreService service;

    @BeforeEach
    void setUp() {
        service = new PopularityScoreService(
              new PopularityWeightsProperties(STARS_WEIGHT, FORKS_WEIGHT, RECENCY_WEIGHT, RECENCY_MAX_DAYS),
              FIXED_CLOCK);
    }

    @Test
    void calculateScore_recentRepo_fullRecencyScore() {
        double score = service.calculateScore(100, 50, NOW);

        double expected = (100 * STARS_WEIGHT) + (50 * FORKS_WEIGHT) + (100 * RECENCY_WEIGHT);
        assertThat(score).isCloseTo(expected, within(0.01));
    }

    @Test
    void calculateScore_repoUpdatedHalfwayThroughMaxDays_halfRecencyScore() {
        long daysAgo = (long) (RECENCY_MAX_DAYS / 2);
        Instant updatedAt = NOW.minus(daysAgo, ChronoUnit.DAYS);
        double expectedRecency = 100 * (1 - daysAgo / RECENCY_MAX_DAYS);

        double score = service.calculateScore(0, 0, updatedAt);

        assertThat(score).isCloseTo(expectedRecency * RECENCY_WEIGHT, within(0.01));
    }

    @Test
    void calculateScore_repoUpdatedAtMaxDays_zeroRecencyScore() {
        Instant updatedAt = NOW.minus((long) RECENCY_MAX_DAYS, ChronoUnit.DAYS);

        double score = service.calculateScore(0, 0, updatedAt);

        assertThat(score).isCloseTo(0.0, within(0.01));
    }

    @Test
    void calculateScore_repoUpdatedBeyondMaxDays_recencyScoreClampsToZero() {
        Instant updatedAt = NOW.minus((long) RECENCY_MAX_DAYS + 100, ChronoUnit.DAYS);

        double score = service.calculateScore(0, 0, updatedAt);

        assertThat(score).isEqualTo(0.0);
    }

    @Test
    void calculateScore_nullUpdatedAt_treatedAsZeroRecency() {
        double score = service.calculateScore(100, 50, null);

        double expected = (100 * STARS_WEIGHT) + (50 * FORKS_WEIGHT);
        assertThat(score).isCloseTo(expected, within(0.01));
    }

    @Test
    void calculateScore_futureUpdatedAt_recencyScoreClampsToHundred() {
        Instant updatedAt = NOW.plus(30, ChronoUnit.DAYS);

        double score = service.calculateScore(0, 0, updatedAt);

        assertThat(score).isEqualTo(100 * RECENCY_WEIGHT);
    }

    @Test
    void calculateScore_starsAndForksWeightedCorrectly() {
        Instant updatedAt = NOW.minus((long) RECENCY_MAX_DAYS, ChronoUnit.DAYS);

        double score = service.calculateScore(200, 80, updatedAt);

        assertThat(score).isCloseTo((200 * STARS_WEIGHT) + (80 * FORKS_WEIGHT), within(0.01));
    }
}
