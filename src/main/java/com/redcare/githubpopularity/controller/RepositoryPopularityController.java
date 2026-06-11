package com.redcare.githubpopularity.controller;

import com.redcare.githubpopularity.api.ScoredRepositoryResponse;
import com.redcare.githubpopularity.service.RepositoryPopularityService;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class RepositoryPopularityController {

    private static final Log log = LogFactory.getLog(RepositoryPopularityController.class);

    private final RepositoryPopularityService repositoryPopularityService;

    public RepositoryPopularityController(final RepositoryPopularityService repositoryPopularityService) {
        this.repositoryPopularityService = repositoryPopularityService;
    }

    @GetMapping("/api/repositories/popular")
    public List<ScoredRepositoryResponse> getRepositoriesByScore(
          @RequestParam @NotBlank final String language,
          @RequestParam @DateTimeFormat(iso = ISO.DATE) final LocalDate createdAfter) {

        log.info("Getting repositories by score for language: " + language + " and created after: " + createdAfter);
        return repositoryPopularityService.getRepositoriesByPopularity(language, createdAfter);
    }


}
