package com.redcare.githubpopularity.exception;

import org.springframework.http.HttpStatusCode;

public class GitHubApiException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final String rateLimitRemaining;
    private final String rateLimitReset;

    public GitHubApiException(String message, HttpStatusCode statusCode,
          String rateLimitRemaining, String rateLimitReset) {
        super(message);
        this.statusCode = statusCode;
        this.rateLimitRemaining = rateLimitRemaining;
        this.rateLimitReset = rateLimitReset;
    }

    public GitHubApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
        this.rateLimitRemaining = null;
        this.rateLimitReset = null;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public String getRateLimitRemaining() {
        return rateLimitRemaining;
    }

    public String getRateLimitReset() {
        return rateLimitReset;
    }
}
