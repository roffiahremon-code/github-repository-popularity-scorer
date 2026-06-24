package com.redcare.githubpopularity.config;

import java.time.Clock;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    private static final Logger log = LoggerFactory.getLogger(RestClientConfig.class);

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public RestClient githubRestClient(final GitHubApiProperties gitHubApiProperties) {
        RestClient.Builder builder = RestClient.builder()
              .baseUrl(gitHubApiProperties.baseUrl())
              .defaultHeader("Accept", "application/vnd.github+json")
              .defaultHeader("X-GitHub-Api-Version", gitHubApiProperties.apiVersion())
              .defaultHeader("User-Agent", "github-repo-popularity-scorer");

        gitHubApiProperties.token()
              .filter(t -> !t.isBlank())
              .ifPresent(t -> builder.defaultHeader("Authorization", "Bearer " + t));

        builder.requestInterceptor((request, body, execution) -> {
            String headers = request.getHeaders().entrySet().stream()
                  .map(e -> e.getKey() + "=" + (e.getKey().equalsIgnoreCase("Authorization") ? "[REDACTED]" : e.getValue()))
                  .collect(Collectors.joining(", "));
            log.info("GitHub request: {} {} headers=[{}]", request.getMethod(), request.getURI(), headers);
            return execution.execute(request, body);
        });

        return builder.build();
    }


}
