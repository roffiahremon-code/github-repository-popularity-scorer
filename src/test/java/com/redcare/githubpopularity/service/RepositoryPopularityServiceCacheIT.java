package com.redcare.githubpopularity.service;

import com.redcare.githubpopularity.client.GitHubRepositoryClient;
import com.redcare.githubpopularity.dto.github.GitHubOwnerDto;
import com.redcare.githubpopularity.dto.github.GitHubRepositoryDto;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class RepositoryPopularityServiceCacheIT {

    private static final LocalDate CREATED_AFTER = LocalDate.of(2024, 1, 1);
    private static final Instant UPDATED_AT = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant CREATED_AT = Instant.parse("2024-01-01T00:00:00Z");

    @Autowired
    private RepositoryPopularityService service;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private GitHubRepositoryClient gitHubRepositoryClient;

    @BeforeEach
    void clearCache() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    void secondIdenticalRequest_doesNotCallGitHub() {
        when(gitHubRepositoryClient.searchRepositories(eq("Java"), any()))
              .thenReturn(List.of(repo("spring", 5000, 500)));

        service.getRepositoriesByPopularity("Java", CREATED_AFTER);
        service.getRepositoriesByPopularity("Java", CREATED_AFTER);

        verify(gitHubRepositoryClient, times(1)).searchRepositories(eq("Java"), any());
    }

    @Test
    void secondIdenticalRequest_returnsSameResult() {
        when(gitHubRepositoryClient.searchRepositories(eq("Java"), any()))
              .thenReturn(List.of(repo("spring", 5000, 500)));

        var first = service.getRepositoriesByPopularity("Java", CREATED_AFTER);
        var second = service.getRepositoriesByPopularity("Java", CREATED_AFTER);

        assertThat(second).isEqualTo(first);
    }

    @Test
    void differentLanguage_callsGitHubAgain() {
        when(gitHubRepositoryClient.searchRepositories(eq("Java"), any()))
              .thenReturn(List.of(repo("spring", 5000, 500)));
        when(gitHubRepositoryClient.searchRepositories(eq("Kotlin"), any()))
              .thenReturn(List.of(repo("ktor", 3000, 200)));

        service.getRepositoriesByPopularity("Java", CREATED_AFTER);
        service.getRepositoriesByPopularity("Kotlin", CREATED_AFTER);

        verify(gitHubRepositoryClient, times(1)).searchRepositories(eq("Java"), any());
        verify(gitHubRepositoryClient, times(1)).searchRepositories(eq("Kotlin"), any());
    }

    @Test
    void languageCacheKey_isCaseInsensitive() {
        when(gitHubRepositoryClient.searchRepositories(any(), any()))
              .thenReturn(List.of(repo("spring", 5000, 500)));

        service.getRepositoriesByPopularity("Java", CREATED_AFTER);
        service.getRepositoriesByPopularity("JAVA", CREATED_AFTER);
        service.getRepositoriesByPopularity("java", CREATED_AFTER);

        verify(gitHubRepositoryClient, times(1)).searchRepositories(any(), any());
    }

    @Test
    void differentCreatedAfter_callsGitHubAgain() {
        when(gitHubRepositoryClient.searchRepositories(eq("Java"), any()))
              .thenReturn(List.of(repo("spring", 5000, 500)));

        service.getRepositoriesByPopularity("Java", LocalDate.of(2024, 1, 1));
        service.getRepositoriesByPopularity("Java", LocalDate.of(2023, 1, 1));

        verify(gitHubRepositoryClient, times(2)).searchRepositories(eq("Java"), any());
    }

    private GitHubRepositoryDto repo(String name, int stars, int forks) {
        return new GitHubRepositoryDto(
              1L, name, "owner/" + name, new GitHubOwnerDto("owner"),
              "https://github.com/owner/" + name, "desc", "Java",
              stars, forks, CREATED_AT, UPDATED_AT);
    }
}
