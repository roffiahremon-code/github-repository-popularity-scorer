# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Run a single test method
./mvnw test -Dtest=ClassName#methodName
```

## Architecture

Spring Boot 4.1 / Java 21 REST API that queries the GitHub Search API and returns repositories ranked by a weighted popularity score.

**Request flow:** `RepositoryPopularityController` → `RepositoryPopularityService` → `GitHubRepositoryClient` → GitHub API, then back through `PopularityScoreService` to produce `ScoredRepositoryResponse`.

**Endpoint:** `GET /api/repositories/popular?language=<lang>&createdAfter=<YYYY-MM-DD>`

**Scoring formula** (`PopularityScoreService`):
```
score = (stars × weightsStars) + (forks × weightsForks) + (recencyScore × weightsRecency)
```
`recencyScore` is a step function (100/75/50/25/0) based on days since last update. Weights are configured in `application.properties` under `popularity.weights.*` (default: stars=0.6, forks=0.3, recency=0.1).

**Config properties** are bound via `@ConfigurationProperties` records:
- `GitHubApiProperties` — GitHub base URL, search path, sort/order/per-page defaults
- `PopularityWeightsProperties` — scoring weights

**`RestClient`** is configured in `RestClientConfig` and injected into `GitHubRepositoryClient`. GitHub API responses are deserialized into `GitHubSearchResponse` / `GitHubRepositoryDto` records; the public API surface uses `ScoredRepositoryResponse`.

`GlobalExceptionHandler` is currently a stub — exception handling is not yet implemented.
