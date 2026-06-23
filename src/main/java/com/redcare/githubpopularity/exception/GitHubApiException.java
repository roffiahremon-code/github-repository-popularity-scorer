package com.redcare.githubpopularity.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class GitHubApiException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final String rateLimitRemaining;
    private final String rateLimitReset;

    public GitHubApiException(final String message, final HttpStatusCode statusCode,
          final String rateLimitRemaining, final String rateLimitReset) {
        super(message);
        this.statusCode = statusCode;
        this.rateLimitRemaining = rateLimitRemaining;
        this.rateLimitReset = rateLimitReset;
    }

    public GitHubApiException(final String message, final Throwable cause) {
        super(message, cause);
        this.statusCode = null;
        this.rateLimitRemaining = null;
        this.rateLimitReset = null;
    }

}
