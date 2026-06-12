# GitHub Repo Popularity Scorer

A Spring Boot REST API that queries the GitHub Search API and returns repositories ranked by a weighted popularity score based on stars, forks, and recency.

## Requirements

- Java 21
- Maven (or use the included `./mvnw` wrapper)

## Running the app

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8081`.

### API documentation

Interactive Swagger UI is available at:

```
http://localhost:8081/swagger-ui/index.html
```

## Endpoint

```
GET /api/repositories/popular?language=<language>&createdAfter=<YYYY-MM-DD>
```

| Parameter      | Type   | Required | Description                                  |
|----------------|--------|----------|----------------------------------------------|
| `language`     | String | Yes      | Programming language to filter by            |
| `createdAfter` | Date   | Yes      | Only include repos created on or after this date (ISO 8601) |

### Example request

```bash
curl "http://localhost:8081/api/repositories/popular?language=Java&createdAfter=2024-01-01"
```

### Example response

```json
{
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "total": 1,
  "repositories": [
    {
      "id": 123456,
      "name": "my-repo",
      "fullName": "owner/my-repo",
      "owner": "owner",
      "description": "An awesome Java project",
      "language": "Java",
      "htmlUrl": "https://github.com/owner/my-repo",
      "stars": 980,
      "forks": 120,
      "createdAt": "2024-03-15T10:00:00Z",
      "updatedAt": "2026-05-01T08:30:00Z",
      "popularityScore": 624.8
    }
  ]
}
```

Results are sorted by `popularityScore` descending.

## Scoring formula

```
score = (stars × starsWeight) + (forks × forksWeight) + (recencyScore × recencyWeight)
```

`recencyScore` decays linearly from 100 (updated today) to 0 (updated `recencyMaxDays` or more days ago). The popularity score is rounded to two decimal places for readability.

### Default weights

| Property                               | Default | Description                              |
|----------------------------------------|---------|------------------------------------------|
| `popularity.weights.stars`             | `0.6`   | Weight applied to star count             |
| `popularity.weights.forks`             | `0.3`   | Weight applied to fork count             |
| `popularity.weights.recency`           | `0.1`   | Weight applied to recency score          |
| `popularity.weights.recency-max-days`  | `365`   | Days after which recency score reaches 0 |

Weights can be overridden in `application.properties` or via environment variables, e.g.:

```bash
POPULARITY_WEIGHTS_STARS=0.5 POPULARITY_WEIGHTS_FORKS=0.4 ./mvnw spring-boot:run
```

## Pagination

The API fetches multiple pages from GitHub to broaden the result set before scoring. This is controlled by two properties:

| Property              | Default | Description                              |
|-----------------------|---------|------------------------------------------|
| `github.api.per-page` | `30`    | Number of results per GitHub API page    |
| `github.api.max-pages`| `3`     | Maximum number of pages to fetch         |

With the defaults, up to **90 repositories** are fetched per request, scored, and returned sorted by popularity score. Pagination stops early if GitHub returns fewer results than `per-page`, indicating no further pages exist.

### Result limit

The maximum number of results is `per-page × max-pages` (default: 90). GitHub's Search API itself caps results at 1,000 per query regardless of pagination, and unauthenticated requests are subject to stricter rate limits. Increasing `max-pages` will improve coverage but increases the number of API calls made per request and the risk of hitting GitHub's secondary rate limit.

## Assumptions and tradeoffs

- The application uses GitHub's public Search API. Without a token the unauthenticated rate limit is 60 requests/hour; with a token it increases to 5,000/hour. Set `GITHUB_TOKEN` to authenticate.
- Scores are calculated in memory after fetching results from GitHub.
- Stars and forks have a larger impact than recency because they are stronger popularity signals.
- Recency is calculated using `updated_at` and decays linearly to 0 over `popularity.weights.recency-max-days` days.
- Results are limited to `per-page × max-pages` repositories. Repositories outside this window are not scored, so the top-ranked result is the most popular within the fetched set, not necessarily across all of GitHub.

## Running the tests

```bash
./mvnw test
```
