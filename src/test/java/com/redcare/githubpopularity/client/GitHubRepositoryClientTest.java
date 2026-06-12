package com.redcare.githubpopularity.client;

import com.redcare.githubpopularity.config.GitHubApiProperties;
import com.redcare.githubpopularity.dto.github.GitHubRepositoryDto;
import com.redcare.githubpopularity.exception.GitHubApiException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.ExpectedCount.once;

class GitHubRepositoryClientTest {

    private static final String BASE_URL = "https://api.github.com";
    private static final String SEARCH_PATH = "/search/repositories";
    private static final LocalDate CREATED_AFTER = LocalDate.of(2024, 1, 1);

    // 3 items = full page (perPage=3), 1 item = partial page → early stop
    private static final int PER_PAGE = 3;

    private MockRestServiceServer server;
    private GitHubRepositoryClient client;

    @BeforeEach
    void setUp() {
        GitHubApiProperties props = new GitHubApiProperties(
                BASE_URL, SEARCH_PATH, "stars", "desc", PER_PAGE, 3, null);

        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new GitHubRepositoryClient(props, builder.build());
    }

    // --- URI construction ---

    @Test
    void searchRepositories_uriContainsLanguageAndCreatedAfterQualifiers() {
        // requestTo receives the percent-encoded URI
        server.expect(once(), requestTo(org.hamcrest.Matchers.allOf(
                    org.hamcrest.Matchers.containsString("language:%22Java%22"),
                    org.hamcrest.Matchers.containsString("created:%3E%3D2024-01-01"))))
              .andRespond(withSuccess(emptyPage(), MediaType.APPLICATION_JSON));

        client.searchRepositories("Java", CREATED_AFTER);
        server.verify();
    }

    @Test
    void searchRepositories_uriContainsSortOrderAndPagination() {
        server.expect(once(), requestTo(org.hamcrest.Matchers.containsString(SEARCH_PATH)))
              .andExpect(queryParam("sort", "stars"))
              .andExpect(queryParam("order", "desc"))
              .andExpect(queryParam("per_page", String.valueOf(PER_PAGE)))
              .andExpect(queryParam("page", "1"))
              .andRespond(withSuccess(emptyPage(), MediaType.APPLICATION_JSON));

        client.searchRepositories("Java", CREATED_AFTER);
        server.verify();
    }

    @Test
    void searchRepositories_languageWithSpaces_quotedInQuery() {
        // space → %20, quotes → %22 in the percent-encoded URI
        server.expect(once(), requestTo(org.hamcrest.Matchers.containsString("language:%22Jupyter%20Notebook%22")))
              .andRespond(withSuccess(emptyPage(), MediaType.APPLICATION_JSON));

        client.searchRepositories("Jupyter Notebook", CREATED_AFTER);
        server.verify();
    }

    // --- Pagination ---

    @Test
    void searchRepositories_fullPages_fetchesAllPages() {
        // pages 1 and 2 are full (PER_PAGE items each), page 3 is empty → stops
        server.expect(once(), queryParam("page", "1"))
              .andRespond(withSuccess(pageOf(PER_PAGE), MediaType.APPLICATION_JSON));
        server.expect(once(), queryParam("page", "2"))
              .andRespond(withSuccess(pageOf(PER_PAGE), MediaType.APPLICATION_JSON));
        server.expect(once(), queryParam("page", "3"))
              .andRespond(withSuccess(emptyPage(), MediaType.APPLICATION_JSON));

        List<GitHubRepositoryDto> result = client.searchRepositories("Java", CREATED_AFTER);

        assertThat(result).hasSize(PER_PAGE * 2);
        server.verify();
    }

    @Test
    void searchRepositories_partialPage_stopsEarly() {
        // page 1 returns fewer than PER_PAGE items → no page 2 expected
        server.expect(once(), queryParam("page", "1"))
              .andRespond(withSuccess(pageOf(1), MediaType.APPLICATION_JSON));

        List<GitHubRepositoryDto> result = client.searchRepositories("Java", CREATED_AFTER);

        assertThat(result).hasSize(1);
        server.verify();
    }

    @Test
    void searchRepositories_emptyFirstPage_returnsEmptyList() {
        server.expect(once(), queryParam("page", "1"))
              .andRespond(withSuccess(emptyPage(), MediaType.APPLICATION_JSON));

        List<GitHubRepositoryDto> result = client.searchRepositories("Java", CREATED_AFTER);

        assertThat(result).isEmpty();
        server.verify();
    }

    @Test
    void searchRepositories_maxPagesReached_doesNotFetchBeyondLimit() {
        // all 3 pages are full → stops at max-pages (3), never requests page 4
        server.expect(once(), queryParam("page", "1"))
              .andRespond(withSuccess(pageOf(PER_PAGE), MediaType.APPLICATION_JSON));
        server.expect(once(), queryParam("page", "2"))
              .andRespond(withSuccess(pageOf(PER_PAGE), MediaType.APPLICATION_JSON));
        server.expect(once(), queryParam("page", "3"))
              .andRespond(withSuccess(pageOf(PER_PAGE), MediaType.APPLICATION_JSON));

        List<GitHubRepositoryDto> result = client.searchRepositories("Java", CREATED_AFTER);

        assertThat(result).hasSize(PER_PAGE * 3);
        server.verify();
    }

    // --- Error mapping ---

    @Test
    void searchRepositories_403Response_throwsGitHubApiException() {
        server.expect(once(), queryParam("page", "1"))
              .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.searchRepositories("Java", CREATED_AFTER))
              .isInstanceOf(GitHubApiException.class)
              .satisfies(ex -> assertThat(((GitHubApiException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void searchRepositories_422Response_throwsGitHubApiException() {
        server.expect(once(), queryParam("page", "1"))
              .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> client.searchRepositories("Java", CREATED_AFTER))
              .isInstanceOf(GitHubApiException.class)
              .satisfies(ex -> assertThat(((GitHubApiException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    void searchRepositories_503Response_throwsGitHubApiException() {
        server.expect(once(), queryParam("page", "1"))
              .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.searchRepositories("Java", CREATED_AFTER))
              .isInstanceOf(GitHubApiException.class)
              .satisfies(ex -> assertThat(((GitHubApiException) ex).getStatusCode())
                    .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void searchRepositories_rateLimitHeaders_capturedOnException() {
        server.expect(once(), queryParam("page", "1"))
              .andRespond(withStatus(HttpStatus.FORBIDDEN)
                    .header("X-RateLimit-Remaining", "0")
                    .header("X-RateLimit-Reset", "1700000000"));

        assertThatThrownBy(() -> client.searchRepositories("Java", CREATED_AFTER))
              .isInstanceOf(GitHubApiException.class)
              .satisfies(ex -> {
                  GitHubApiException gae = (GitHubApiException) ex;
                  assertThat(gae.getRateLimitRemaining()).isEqualTo("0");
                  assertThat(gae.getRateLimitReset()).isEqualTo("1700000000");
              });
    }

    // --- Helpers ---

    private String emptyPage() {
        return """
                {"total_count":0,"incomplete_results":false,"items":[]}
                """;
    }

    private String pageOf(int count) {
        StringBuilder items = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) items.append(",");
            items.append("""
                    {"id":%d,"name":"repo-%d","full_name":"owner/repo-%d",
                     "owner":{"login":"owner"},"html_url":"https://github.com/owner/repo-%d",
                     "description":"desc","language":"Java",
                     "stargazers_count":100,"forks_count":10,
                     "created_at":"2024-01-01T00:00:00Z","updated_at":"2025-01-01T00:00:00Z"}
                    """.formatted(i, i, i, i));
        }
        return """
                {"total_count":%d,"incomplete_results":false,"items":[%s]}
                """.formatted(count, items);
    }
}
