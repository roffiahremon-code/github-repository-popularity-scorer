package com.redcare.githubpopularity.api;

import java.util.List;

public record PopularRepositoriesResponse(
      String correlationId,
      int total,
      List<ScoredRepositoryResponse> repositories
) {

}
