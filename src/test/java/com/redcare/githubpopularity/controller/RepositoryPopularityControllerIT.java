package com.redcare.githubpopularity.controller;

import com.redcare.githubpopularity.client.GitHubRepositoryClient;
import com.redcare.githubpopularity.dto.github.GitHubOwnerDto;
import com.redcare.githubpopularity.dto.github.GitHubRepositoryDto;
import com.redcare.githubpopularity.exception.GitHubApiException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RepositoryPopularityControllerIT {

    private static final String URL = "/api/repositories/popular";
    private static final Instant UPDATED_AT = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant CREATED_AT = Instant.parse("2024-01-01T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GitHubRepositoryClient gitHubRepositoryClient;

    @Test
    void getRepositoriesByScore_validRequest_returns200WithCorrelationId() throws Exception {
        when(gitHubRepositoryClient.searchRepositories(eq("Java"), any()))
              .thenReturn(List.of(repo(1L, "spring", 5000, 500)));

        mockMvc.perform(get(URL)
                    .param("language", "Java")
                    .param("createdAfter", "2024-01-01")
                    .header("X-Correlation-ID", "a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
              .andExpect(status().isOk())
              .andExpect(header().string("X-Correlation-ID", "a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
              .andExpect(jsonPath("$.correlationId").value("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
              .andExpect(jsonPath("$.total").value(1))
              .andExpect(jsonPath("$.repositories", hasSize(1)))
              .andExpect(jsonPath("$.repositories[0].fullName").value("owner/spring"))
              .andExpect(jsonPath("$.repositories[0].popularityScore").isNumber());
    }

    @Test
    void getRepositoriesByScore_noCorrelationIdHeader_generatesOne() throws Exception {
        when(gitHubRepositoryClient.searchRepositories(eq("Java"), any()))
              .thenReturn(List.of());

        mockMvc.perform(get(URL)
                    .param("language", "Java")
                    .param("createdAfter", "2024-01-01"))
              .andExpect(status().isOk())
              .andExpect(header().string("X-Correlation-ID", notNullValue()))
              .andExpect(jsonPath("$.correlationId", notNullValue()));
    }

    @Test
    void getRepositoriesByScore_multipleRepos_sortedByScoreDescending() throws Exception {
        when(gitHubRepositoryClient.searchRepositories(eq("Java"), any()))
              .thenReturn(List.of(
                    repo(1L, "low-score", 10, 5),
                    repo(2L, "high-score", 5000, 500)
              ));

        mockMvc.perform(get(URL)
                    .param("language", "Java")
                    .param("createdAfter", "2024-01-01"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.repositories[0].fullName").value("owner/high-score"))
              .andExpect(jsonPath("$.repositories[1].fullName").value("owner/low-score"));
    }

    @Test
    void getRepositoriesByScore_missingLanguage_returns400() throws Exception {
        mockMvc.perform(get(URL)
                    .param("createdAfter", "2024-01-01"))
              .andExpect(status().isBadRequest());
    }

    @Test
    void getRepositoriesByScore_missingCreatedAfter_returns400() throws Exception {
        mockMvc.perform(get(URL)
                    .param("language", "Java"))
              .andExpect(status().isBadRequest());
    }

    @Test
    void getRepositoriesByScore_createdAfterBeforeMinYear_returns400() throws Exception {
        mockMvc.perform(get(URL)
                    .param("language", "Java")
                    .param("createdAfter", "2007-12-31"))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.correlationId", notNullValue()));
    }

    @Test
    void getRepositoriesByScore_invalidLanguage_returns400() throws Exception {
        mockMvc.perform(get(URL)
                    .param("language", "Java forks:>1000")
                    .param("createdAfter", "2024-01-01"))
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.correlationId", notNullValue()));
    }

    @Test
    void getRepositoriesByScore_githubApiError_returns502WithCorrelationId() throws Exception {
        when(gitHubRepositoryClient.searchRepositories(eq("Java"), any()))
              .thenThrow(new GitHubApiException("GitHub API error: 503", HttpStatus.SERVICE_UNAVAILABLE, null, null));

        mockMvc.perform(get(URL)
                    .param("language", "Java")
                    .param("createdAfter", "2024-01-01"))
              .andExpect(status().isBadGateway())
              .andExpect(jsonPath("$.correlationId", notNullValue()))
              .andExpect(jsonPath("$.detail").exists());
    }

    private GitHubRepositoryDto repo(Long id, String name, int stars, int forks) {
        return new GitHubRepositoryDto(
              id, name, "owner/" + name, new GitHubOwnerDto("owner"),
              "https://github.com/owner/" + name, "A description", "Java",
              stars, forks, CREATED_AT, UPDATED_AT);
    }
}
