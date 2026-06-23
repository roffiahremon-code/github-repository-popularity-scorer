package com.redcare.githubpopularity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache.github-repositories")
public record CacheProperties(
      long maximumSize,
      long expireAfterWriteMinutes
) {

}
