package com.redcare.githubpopularity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github.api")
public record GitHubApiProperties(
      String baseUrl,
      String searchRepoPath,
      String sort,
      String order,
      int perPage
) {

}
