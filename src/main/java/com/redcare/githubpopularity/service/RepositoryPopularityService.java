package com.redcare.githubpopularity.service;

import com.redcare.githubpopularity.api.ScoredRepositoryResponse;
import com.redcare.githubpopularity.client.GitHubRepositoryClient;
import com.redcare.githubpopularity.dto.github.GitHubRepositoryDto;
import com.redcare.githubpopularity.dto.github.GitHubSearchResponse;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service
public class RepositoryPopularityService {

    private static final Log log = LogFactory.getLog(RepositoryPopularityService.class);

    private final GitHubRepositoryClient gitHubRepositoryClient;
    private final PopularityScoreService popularityScoreService;

    public RepositoryPopularityService(final GitHubRepositoryClient gitHubRepositoryClient, final PopularityScoreService popularityScoreService) {
        this.gitHubRepositoryClient = gitHubRepositoryClient;
        this.popularityScoreService = popularityScoreService;
    }

    public List<ScoredRepositoryResponse> getRepositoriesByPopularity(final String language, final LocalDate createdAfter) {
        log.debug("Fetching repositories for language=" + language + ", createdAfter=" + createdAfter);

        GitHubSearchResponse response = gitHubRepositoryClient.searchRepositories(language, createdAfter);

        if (response == null || response.items() == null) {
            log.warn("GitHub API returned null response for language=" + language + ", createdAfter=" + createdAfter);
            return List.of();
        }

        log.debug("GitHub API returned " + response.items().size() + " repositories");

        List<ScoredRepositoryResponse> results = response.items()
              .stream()
              .map(this::createScoredResponse)
              .sorted(Comparator.comparingDouble(ScoredRepositoryResponse::popularityScore).reversed())
              .toList();

        log.debug("Returning " + results.size() + " scored repositories");
        return results;
    }

    private ScoredRepositoryResponse createScoredResponse(final GitHubRepositoryDto repository) {
        double score = popularityScoreService.calculateScore(repository.stargazersCount(), repository.forksCount(), repository.updatedAt());
        log.debug("Scored repository " + repository.fullName() + ": score=" + score
              + " (stars=" + repository.stargazersCount() + ", forks=" + repository.forksCount() + ", updatedAt=" + repository.updatedAt() + ")");
        return new ScoredRepositoryResponse(
              repository.id(),
              repository.name(),
              repository.fullName(),
              repository.owner() != null ? repository.owner().login() : null,
              repository.description(),
              repository.language(),
              repository.htmlUrl(),
              repository.stargazersCount(),
              repository.forksCount(),
              repository.createdAt(),
              repository.updatedAt(),
              score
        );
    }
}
