package com.redcare.githubpopularity.service;

import com.redcare.githubpopularity.api.ScoredRepositoryResponse;
import com.redcare.githubpopularity.client.GitHubRepositoryClient;
import com.redcare.githubpopularity.dto.github.GitHubRepositoryDto;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.redcare.githubpopularity.config.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryPopularityService {

    private final GitHubRepositoryClient gitHubRepositoryClient;
    private final PopularityScoreService popularityScoreService;

    @Cacheable(
          value = CacheConfig.GITHUB_REPOSITORIES,
          key = "#language.toLowerCase() + '_' + #createdAfter"
    )
    public List<ScoredRepositoryResponse> getRepositoriesByPopularity(final String language, final LocalDate createdAfter) {
        log.debug("Fetching repositories for language={}, createdAfter={}", language, createdAfter);

        final List<GitHubRepositoryDto> items = gitHubRepositoryClient.searchRepositories(language, createdAfter);

        log.debug("GitHub API returned {} repositories", items.size());

        final List<ScoredRepositoryResponse> results = items
              .stream()
              .map(this::createScoredResponse)
              .sorted(Comparator.comparingDouble(ScoredRepositoryResponse::popularityScore).reversed())
              .toList();

        log.debug("Returning {} scored repositories", results.size());
        return results;
    }

    private ScoredRepositoryResponse createScoredResponse(final GitHubRepositoryDto repository) {
        final double score = popularityScoreService.calculateScore(repository.stargazersCount(), repository.forksCount(), repository.updatedAt());
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
