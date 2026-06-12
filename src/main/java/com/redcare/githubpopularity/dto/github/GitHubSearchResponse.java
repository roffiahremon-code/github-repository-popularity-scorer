package com.redcare.githubpopularity.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubSearchResponse(

      @JsonProperty("total_count")
      int totalCount,

      @JsonProperty("incomplete_results")
      boolean incompleteResults,

      @JsonSetter(nulls = Nulls.AS_EMPTY)
      List<GitHubRepositoryDto> items
) {

}
