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
        return RestClient.builder()
              .baseUrl(gitHubApiProperties.baseUrl())
              .defaultHeader("Accept", "application/vnd.github+json")
              .defaultHeader("User-Agent", "github-repo-popularity-scorer")
              .build();
    }
}
