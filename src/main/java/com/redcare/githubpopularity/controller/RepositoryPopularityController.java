package com.redcare.githubpopularity.controller;

import com.redcare.githubpopularity.api.PopularRepositoriesResponse;
import com.redcare.githubpopularity.api.ScoredRepositoryResponse;
import com.redcare.githubpopularity.service.RepositoryPopularityService;
import com.redcare.githubpopularity.validation.MinYear;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
public class RepositoryPopularityController {

    private static final Logger log = LoggerFactory.getLogger(RepositoryPopularityController.class);

    private final RepositoryPopularityService repositoryPopularityService;

    public RepositoryPopularityController(final RepositoryPopularityService repositoryPopularityService) {
        this.repositoryPopularityService = repositoryPopularityService;
    }

    @GetMapping("/api/repositories/popular")
    public PopularRepositoriesResponse getRepositoriesByScore(
          @RequestParam @NotBlank @Pattern(regexp = "[\\w.+#\\- ]+", message = "must contain only letters, digits, spaces, or the characters . + # -") final String language,
          @RequestParam @PastOrPresent @MinYear(2008) @DateTimeFormat(iso = ISO.DATE) final LocalDate createdAfter) {

        log.info("Getting repositories by score for language: {} and created after: {}", language, createdAfter);
        List<ScoredRepositoryResponse> repositories = repositoryPopularityService.getRepositoriesByPopularity(language, createdAfter);
        return new PopularRepositoriesResponse(MDC.get("correlationId"), repositories.size(), repositories);
    }


}
