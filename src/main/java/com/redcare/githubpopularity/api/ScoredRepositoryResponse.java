package com.redcare.githubpopularity.api;

import java.time.Instant;

public record ScoredRepositoryResponse(
      Long id,
      String name,
      String fullName,
      String owner,
      String description,
      String language,
      String htmlUrl,
      int stars,
      int forks,
      Instant createdAt,
      Instant updatedAt,
      double popularityScore
) {

}
