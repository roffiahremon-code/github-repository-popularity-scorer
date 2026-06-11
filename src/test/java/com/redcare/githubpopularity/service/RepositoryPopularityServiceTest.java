package com.redcare.githubpopularity.service;

import com.redcare.githubpopularity.api.ScoredRepositoryResponse;
import com.redcare.githubpopularity.client.GitHubRepositoryClient;
import com.redcare.githubpopularity.dto.github.GitHubOwnerDto;
import com.redcare.githubpopularity.dto.github.GitHubRepositoryDto;
import com.redcare.githubpopularity.dto.github.GitHubSearchResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryPopularityServiceTest {

    @Mock
    private GitHubRepositoryClient gitHubRepositoryClient;

    @Mock
    private PopularityScoreService popularityScoreService;

    private RepositoryPopularityService service;

    private static final LocalDate CREATED_AFTER = LocalDate.of(2024, 1, 1);
    private static final Instant CREATED_AT = Instant.parse("2024-03-01T00:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        service = new RepositoryPopularityService(gitHubRepositoryClient, popularityScoreService);
    }

    @Test
    void getRepositoriesByPopularity_nullResponse_returnsEmptyList() {
        when(gitHubRepositoryClient.searchRepositories("Java", CREATED_AFTER)).thenReturn(null);

        List<ScoredRepositoryResponse> result = service.getRepositoriesByPopularity("Java", CREATED_AFTER);

        assertThat(result).isEmpty();
    }

    @Test
    void getRepositoriesByPopularity_nullItems_returnsEmptyList() {
        when(gitHubRepositoryClient.searchRepositories("Java", CREATED_AFTER))
              .thenReturn(new GitHubSearchResponse(0, false, null));

        List<ScoredRepositoryResponse> result = service.getRepositoriesByPopularity("Java", CREATED_AFTER);

        assertThat(result).isEmpty();
    }

    @Test
    void getRepositoriesByPopularity_multipleRepos_sortedByScoreDescending() {
        GitHubRepositoryDto lowScoreRepo = repo(1L, "low-score", 10, 5);
        GitHubRepositoryDto highScoreRepo = repo(2L, "high-score", 500, 200);
        when(gitHubRepositoryClient.searchRepositories("Java", CREATED_AFTER))
              .thenReturn(new GitHubSearchResponse(2, false, List.of(lowScoreRepo, highScoreRepo)));
        when(popularityScoreService.calculateScore(10, 5, UPDATED_AT)).thenReturn(20.0);
        when(popularityScoreService.calculateScore(500, 200, UPDATED_AT)).thenReturn(360.0);

        List<ScoredRepositoryResponse> result = service.getRepositoriesByPopularity("Java", CREATED_AFTER);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).popularityScore()).isEqualTo(360.0);
        assertThat(result.get(1).popularityScore()).isEqualTo(20.0);
    }

    @Test
    void getRepositoriesByPopularity_mapsAllFieldsCorrectly() {
        GitHubRepositoryDto repoDto = repo(42L, "my-repo", 100, 50);
        when(gitHubRepositoryClient.searchRepositories("Java", CREATED_AFTER))
              .thenReturn(new GitHubSearchResponse(1, false, List.of(repoDto)));
        when(popularityScoreService.calculateScore(100, 50, UPDATED_AT)).thenReturn(75.0);

        ScoredRepositoryResponse result = service.getRepositoriesByPopularity("Java", CREATED_AFTER).getFirst();

        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.name()).isEqualTo("my-repo");
        assertThat(result.fullName()).isEqualTo("owner/my-repo");
        assertThat(result.owner()).isEqualTo("owner");
        assertThat(result.stars()).isEqualTo(100);
        assertThat(result.forks()).isEqualTo(50);
        assertThat(result.description()).isEqualTo("A description");
        assertThat(result.language()).isEqualTo("Java");
        assertThat(result.htmlUrl()).isEqualTo("https://github.com/owner/my-repo");
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
        assertThat(result.updatedAt()).isEqualTo(UPDATED_AT);
        assertThat(result.popularityScore()).isEqualTo(75.0);
        verify(popularityScoreService).calculateScore(100, 50, UPDATED_AT);
    }

    @Test
    void getRepositoriesByPopularity_nullOwner_mapsOwnerAsNull() {
        GitHubRepositoryDto repoDto = new GitHubRepositoryDto(
              1L, "no-owner", "owner/no-owner", null, "https://github.com/owner/no-owner",
              "desc", "Java", 10, 5, CREATED_AT, UPDATED_AT);
        when(gitHubRepositoryClient.searchRepositories("Java", CREATED_AFTER))
              .thenReturn(new GitHubSearchResponse(1, false, List.of(repoDto)));
        when(popularityScoreService.calculateScore(10, 5, UPDATED_AT)).thenReturn(10.0);

        ScoredRepositoryResponse result = service.getRepositoriesByPopularity("Java", CREATED_AFTER).getFirst();

        assertThat(result.owner()).isNull();
    }

    private GitHubRepositoryDto repo(Long id, String name, int stars, int forks) {
        return new GitHubRepositoryDto(
              id, name, "owner/" + name, new GitHubOwnerDto("owner"),
              "https://github.com/owner/" + name, "A description", "Java",
              stars, forks, CREATED_AT, UPDATED_AT);
    }
}
