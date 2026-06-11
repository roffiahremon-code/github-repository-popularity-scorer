package com.redcare.githubpopularity.exception;

import jakarta.validation.ConstraintViolationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Log log = LogFactory.getLog(GlobalExceptionHandler.class);

    @ExceptionHandler
    ProblemDetail handleGitHubApiException(final GitHubApiException e) {
        log.error("GitHub API error", e);
        HttpStatus status = e.getStatusCode() != null
              ? HttpStatus.BAD_GATEWAY
              : HttpStatus.SERVICE_UNAVAILABLE;

        return ProblemDetail.forStatusAndDetail(status, e.getMessage());
    }

    @ExceptionHandler({
          ConstraintViolationException.class,
          MethodArgumentTypeMismatchException.class,
          MissingServletRequestParameterException.class
    })
    ProblemDetail handleBadRequest(final Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
