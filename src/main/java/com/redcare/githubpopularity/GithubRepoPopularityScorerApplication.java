package com.redcare.githubpopularity;

import com.redcare.githubpopularity.config.GitHubApiProperties;
import com.redcare.githubpopularity.config.PopularityWeightsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
	PopularityWeightsProperties.class,
	GitHubApiProperties.class
})
public class GithubRepoPopularityScorerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GithubRepoPopularityScorerApplication.class, args);
	}

}
