package com.redcare.githubpopularity.client;

import com.redcare.githubpopularity.config.GitHubApiProperties;
import com.redcare.githubpopularity.dto.github.GitHubSearchResponse;
import com.redcare.githubpopularity.exception.GitHubApiException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class GitHubRepositoryClient {

    private final GitHubApiProperties gitHubApiProperties;
    private final RestClient gitHubRestClient;

    public GitHubRepositoryClient(final GitHubApiProperties gitHubApiProperties, final RestClient gitHubRestClient) {
        this.gitHubApiProperties = gitHubApiProperties;
        this.gitHubRestClient = gitHubRestClient;
    }

    public GitHubSearchResponse searchRepositories(final String language, final LocalDate createdAfter) {
        try {
            return gitHubRestClient.get()
                  .uri(uriBuilder -> uriBuilder
                        .path(gitHubApiProperties.searchRepoPath())
                        .queryParam("q", buildSearchQuery(language, createdAfter))
                        .queryParam("sort", gitHubApiProperties.sort())
                        .queryParam("order", gitHubApiProperties.order())
                        .queryParam("per_page", gitHubApiProperties.perPage())
                        .build())
                  .retrieve()
                  .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        (request, response) -> {
                            throw new GitHubApiException(
                                  "GitHub API error: " + response.getStatusCode(),
                                  response.getStatusCode());
                        })
                  .body(GitHubSearchResponse.class);
        } catch (ResourceAccessException e) {
            throw new GitHubApiException("GitHub API is unreachable", e);
        }
    }

    private String buildSearchQuery(final String language, final LocalDate createdAfter) {
        final List<String> qualifiers = new ArrayList<>();
        qualifiers.add("language:" + language);
        qualifiers.add("created:>=" + createdAfter);
        return String.join(" ", qualifiers);
    }


}
