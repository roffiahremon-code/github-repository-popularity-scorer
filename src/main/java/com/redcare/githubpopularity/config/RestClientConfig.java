package com.redcare.githubpopularity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient githubRestClient(GitHubApiProperties gitHubApiProperties) {
        return RestClient.builder()
              .baseUrl(gitHubApiProperties.baseUrl())
              .build();
    }
}
