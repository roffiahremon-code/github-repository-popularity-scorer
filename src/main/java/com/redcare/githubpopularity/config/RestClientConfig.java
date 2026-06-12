package com.redcare.githubpopularity.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public RestClient githubRestClient(GitHubApiProperties gitHubApiProperties) {
        RestClient.Builder builder = RestClient.builder()
              .baseUrl(gitHubApiProperties.baseUrl())
              .defaultHeader("Accept", "application/vnd.github+json")
              .defaultHeader("User-Agent", "github-repo-popularity-scorer");

        if (hasToken(gitHubApiProperties)) {
            builder.defaultHeader("Authorization", "Bearer " + gitHubApiProperties.token());
        }

        return builder.build();
    }

    private boolean hasToken(GitHubApiProperties gitHubApiProperties) {
        return gitHubApiProperties.token() != null && !gitHubApiProperties.token().isBlank();
    }
}
