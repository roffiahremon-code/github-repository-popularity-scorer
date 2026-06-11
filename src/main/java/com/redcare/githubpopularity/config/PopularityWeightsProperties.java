package com.redcare.githubpopularity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "popularity.weights")
public record PopularityWeightsProperties(double stars, double forks, double recency){

}
