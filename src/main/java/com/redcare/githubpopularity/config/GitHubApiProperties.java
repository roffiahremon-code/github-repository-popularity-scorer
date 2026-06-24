package com.redcare.githubpopularity.config;

import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github.api")
public record GitHubApiProperties(
      String baseUrl,
      String searchRepoPath,
      String apiVersion,
      String sort,
      String order,
      int perPage,
      int maxPages,
      Optional<String> token
) {

}
