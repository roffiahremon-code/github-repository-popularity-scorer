package com.redcare.githubpopularity.service;

import com.redcare.githubpopularity.api.ScoredRepositoryResponse;
import com.redcare.githubpopularity.client.GitHubRepositoryClient;
import com.redcare.githubpopularity.dto.github.GitHubRepositoryDto;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RepositoryPopularityService {

    private static final Logger log = LoggerFactory.getLogger(RepositoryPopularityService.class);

    private final GitHubRepositoryClient gitHubRepositoryClient;
    private final PopularityScoreService popularityScoreService;

    public RepositoryPopularityService(final GitHubRepositoryClient gitHubRepositoryClient,
          final PopularityScoreService popularityScoreService) {
        this.gitHubRepositoryClient = gitHubRepositoryClient;
        this.popularityScoreService = popularityScoreService;
    }

    public List<ScoredRepositoryResponse> getRepositoriesByPopularity(final String language, final LocalDate createdAfter) {
        log.debug("Fetching repositories for language={}, createdAfter={}", language, createdAfter);

        List<GitHubRepositoryDto> items = gitHubRepositoryClient.searchRepositories(language, createdAfter);

        log.debug("GitHub API returned {} repositories", items.size());

        List<ScoredRepositoryResponse> results = items
              .stream()
              .map(this::createScoredResponse)
              .sorted(Comparator.comparingDouble(ScoredRepositoryResponse::popularityScore).reversed())
              .toList();

        log.debug("Returning {} scored repositories", results.size());
        return results;
    }

    private ScoredRepositoryResponse createScoredResponse(final GitHubRepositoryDto repository) {
        double score = popularityScoreService.calculateScore(repository.stargazersCount(), repository.forksCount(), repository.updatedAt());
        log.debug("Scored repository {}: score={} (stars={}, forks={}, updatedAt={})",
              repository.fullName(), score, repository.stargazersCount(), repository.forksCount(), repository.updatedAt());
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
