package com.redcare.githubpopularity.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubRepositoryDto(

      Long id,

      String name,

      @JsonProperty("full_name")
      String fullName,

      GitHubOwnerDto owner,

      @JsonProperty("html_url")
      String htmlUrl,

      String description,

      String language,

      @JsonProperty("stargazers_count")
      int stargazersCount,

      @JsonProperty("forks_count")
      int forksCount,

      @JsonProperty("created_at")
      Instant createdAt,

      @JsonProperty("updated_at")
      Instant updatedAt
) {

}
