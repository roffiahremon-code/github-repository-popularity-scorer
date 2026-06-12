package com.redcare.githubpopularity.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler
    ProblemDetail handleGitHubApiException(final GitHubApiException e) {
        log.error("GitHub API error", e);
        HttpStatus status = e.getStatusCode() != null
              ? HttpStatus.BAD_GATEWAY
              : HttpStatus.SERVICE_UNAVAILABLE;

        return withCorrelationId(ProblemDetail.forStatusAndDetail(status, e.getMessage()));
    }

    @ExceptionHandler({
          ConstraintViolationException.class,
          MethodArgumentTypeMismatchException.class,
          MissingServletRequestParameterException.class
    })
    ProblemDetail handleBadRequest(final Exception e) {
        log.warn("Bad request: {}", e.getMessage());
        return withCorrelationId(ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    private ProblemDetail withCorrelationId(final ProblemDetail problem) {
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            problem.setProperty("correlationId", correlationId);
        }
        return problem;
    }
}
