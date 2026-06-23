package com.redcare.githubpopularity.client;

import com.redcare.githubpopularity.config.GitHubApiProperties;
import com.redcare.githubpopularity.dto.github.GitHubRepositoryDto;
import com.redcare.githubpopularity.dto.github.GitHubSearchResponse;
import com.redcare.githubpopularity.exception.GitHubApiException;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Component
@Slf4j
@RequiredArgsConstructor
public class GitHubRepositoryClient {

    private static final String X_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final String X_RATE_LIMIT_RESET = "X-RateLimit-Reset";

    private final GitHubApiProperties gitHubApiProperties;
    private final RestClient gitHubRestClient;

    public List<GitHubRepositoryDto> searchRepositories(final String language, final LocalDate createdAfter) {
        final List<GitHubRepositoryDto> allItems = new ArrayList<>();
        final long startTime = System.nanoTime();

        boolean morePages = true;
        for (int page = 1; page <= gitHubApiProperties.maxPages() && morePages; page++) {

            final GitHubSearchResponse response = fetchRepositoryPage(language, createdAfter, page);

            if (hasNoItems(response)) {
                log.debug("Page {} returned no items, stopping pagination", page);
                morePages = false;
            } else {
                allItems.addAll(response.items());
                log.debug("Fetched page {}/{}: {} items (total so far: {})",
                      page, gitHubApiProperties.maxPages(), response.items().size(), allItems.size());

                if (isLastPage(response.items())) {
                    log.debug("Page {} was a partial page, no more results available", page);
                    morePages = false;
                }
            }
        }

        log.info("GitHub search completed: {} repositories fetched in {}ms", allItems.size(), elapsedMillis(startTime));
        return allItems;
    }

    private boolean hasNoItems(final GitHubSearchResponse response) {
        return response == null || response.items().isEmpty();
    }

    private GitHubSearchResponse fetchRepositoryPage(final String language, final LocalDate createdAfter, final int page) {
        final long startTime = System.nanoTime();

        try {
            final GitHubSearchResponse response = gitHubRestClient.get()
                  .uri(uriBuilder -> buildSearchUri(uriBuilder, language, createdAfter, page))
                  .retrieve()
                  .onStatus(HttpStatusCode::isError, this::handleGitHubError)
                  .body(GitHubSearchResponse.class);

            log.debug("GitHub API page {} completed in {}ms", page, elapsedMillis(startTime));
            return response;

        } catch (final GitHubApiException e) {
            logRateLimitDetails(e);
            throw e;

        } catch (final ResourceAccessException e) {
            log.warn("GitHub API call failed for page {}", page);
            throw new GitHubApiException("GitHub API is unreachable", e);
        }

    }

    private long elapsedMillis(final long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    private void logRateLimitDetails(final GitHubApiException e) {
        if (e.getRateLimitRemaining() != null || e.getRateLimitReset() != null) {
            log.error("GitHub API error {} — rate limit headers present: {}={}, {}={}",
                  e.getStatusCode(),
                  X_RATE_LIMIT_REMAINING, e.getRateLimitRemaining(),
                  X_RATE_LIMIT_RESET, e.getRateLimitReset());
        }
    }

    private URI buildSearchUri(final UriBuilder uriBuilder, final String language, final LocalDate createdAfter, final int currentPage) {
        return uriBuilder
              .path(gitHubApiProperties.searchRepoPath())
              .queryParam("q", buildSearchQuery(language, createdAfter))
              .queryParam("sort", gitHubApiProperties.sort())
              .queryParam("order", gitHubApiProperties.order())
              .queryParam("per_page", gitHubApiProperties.perPage())
              .queryParam("page", currentPage)
              .build();
    }

    private void handleGitHubError(final HttpRequest httpRequest, final ClientHttpResponse response) throws IOException {
        final String rateLimitRemaining = response.getHeaders().getFirst(X_RATE_LIMIT_REMAINING);
        final String rateLimitReset = response.getHeaders().getFirst(X_RATE_LIMIT_RESET);
        final String body = new String(response.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        log.error("GitHub API error: method={} uri={} status={} body={}",
              httpRequest.getMethod(), httpRequest.getURI(), response.getStatusCode(), body);
        throw new GitHubApiException(
              "GitHub API error: " + response.getStatusCode(),
              response.getStatusCode(),
              rateLimitRemaining,
              rateLimitReset
        );
    }

    private String buildSearchQuery(final String language, final LocalDate createdAfter) {
        final List<String> qualifiers = new ArrayList<>();
        qualifiers.add("language:\"" + language.replace("\"", "\\\"") + "\"");
        qualifiers.add("created:>=" + createdAfter);
        return String.join(" ", qualifiers);
    }

    private boolean isLastPage(final List<GitHubRepositoryDto> pageItems) {
        return pageItems.size() < gitHubApiProperties.perPage();
    }

}
