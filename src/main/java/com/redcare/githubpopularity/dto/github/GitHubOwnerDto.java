package com.redcare.githubpopularity.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubOwnerDto(

      String login
) {

}
