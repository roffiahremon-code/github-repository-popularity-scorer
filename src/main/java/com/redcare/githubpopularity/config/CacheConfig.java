package com.redcare.githubpopularity.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CacheConfig {

    public static final String GITHUB_REPOSITORIES = "github-repositories";

    private final CacheProperties cacheProperties;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(GITHUB_REPOSITORIES);
        cacheManager.setCaffeine(Caffeine.newBuilder()
              .maximumSize(cacheProperties.maximumSize())
              .expireAfterWrite(cacheProperties.expireAfterWriteMinutes(), TimeUnit.MINUTES));
        return cacheManager;
    }
}
