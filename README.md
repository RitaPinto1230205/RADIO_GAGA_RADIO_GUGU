# Radio Gaga

A small radio studio in the browser, backed by a Spring Boot REST API. You create stations, search for music through the Deezer API and play 30-second previews on a turntable.

I built it to practise backend fundamentals in Java: REST design, error handling, configuration through environment variables, API security, and running a service the way it would run in production: in containers, with health checks, metrics and a load test.

![Radio Gaga playing a track](docs/Captura%20de%20ecrã%202026-10-12,%20às%2000.37.42.png)

![Radio Gaga playing a track](docs/Captura%20de%20ecrã%202026-10-13,%20às%2010.27.51.png)

## Stack

- Java 21, Spring Boot 4 (Web, Validation, Data JPA, Actuator)
- PostgreSQL 17 in Docker, H2 in memory for local runs and tests
- Docker and Docker Compose
- Micrometer with a Prometheus endpoint for metrics
- k6 for load testing
- Deezer public API for search and previews
- Plain HTML, CSS and JavaScript for the frontend, served by Spring Boot

## Run it with Docker

You need Docker Desktop.

```bash
git clone git@github.com:RitaPinto1230205/RADIO_GAGA_RADIO_GUGU.git
cd RADIO_GAGA_RADIO_GUGU

# secrets for your machine only, never committed
echo "RADIOGAGA_API_KEY=$(openssl rand -hex 32)" > .env
echo "DATABASE_PASSWORD=$(openssl rand -hex 16)" >> .env

docker compose up --build -d
```

Then open http://localhost:8080.

This starts two containers: the app and PostgreSQL. Data is kept in a Docker volume, so stations survive restarts. `docker compose down` stops everything and keeps the data; `docker compose down -v` also deletes it.

## Run it without Docker

You need Java 21 or newer. Create the `.env` file as above, then:

```bash
./mvnw spring-boot:run
```

Without the database variables the app falls back to H2 in memory, so data is lost when it stops. Four default stations are created at startup.

## Tests

```bash
./mvnw test
```

Integration tests start the full application and check the API key rules (401 without or with a wrong key, 201 with a valid one, public reads), input validation and the 404 response.

## Configuration

| Variable | Required | Default | Purpose |
|---|---|---|---|
| `RADIOGAGA_API_KEY` | yes | none | Key required by the write endpoints. The app refuses to start without it. |
| `DATABASE_PASSWORD` | with Docker | none | PostgreSQL password, shared by both containers |
| `DATABASE_URL` | no | H2 in memory | JDBC URL. Docker Compose points it at PostgreSQL. |
| `DATABASE_USERNAME` | no | `sa` | Database user |
| `DEEZER_BASE_URL` | no | `https://api.deezer.com` | Base URL of the music provider |
| `DEEZER_TIMEOUT_SECONDS` | no | `3` | Connect and read timeout for calls to Deezer |

Values are read from the environment, or from a local `.env` file that is ignored by git. `.env.example` lists the variables without values.

## API

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/stations` | none | List all stations |
| `GET` | `/stations/{id}` | none | Get one station |
| `POST` | `/stations` | API key | Create a station |
| `DELETE` | `/stations/{id}` | API key | Delete a station |
| `GET` | `/search?q=` | none | Search tracks on Deezer |

Write requests send the key in the `X-API-Key` header:

```bash
curl -X POST http://localhost:8080/stations \
  -H "Content-Type: application/json" \
  -H "X-API-Key: <your key>" \
  -d '{"name":"Classic Rock","description":"Queen and friends"}'
```

Errors use the Problem Details format (RFC 9457):

```json
{
  "title": "Not Found",
  "status": 404,
  "detail": "Station 999 not found",
  "instance": "/stations/999"
}
```

## Monitoring

Spring Boot Actuator runs on a separate port, 8081, which Docker Compose binds to `127.0.0.1` only. The public port 8080 never exposes it.

| Endpoint | What it shows |
|---|---|
| http://localhost:8081/actuator/health | App status, database connectivity, liveness and readiness |
| http://localhost:8081/actuator/metrics | Request counts and latency, JVM memory and threads, database connection pool |
| http://localhost:8081/actuator/prometheus | The same metrics in the format Prometheus scrapes |

`docker stats` shows CPU and memory per container while the app runs.

## Load testing

Two k6 scripts in `load-test/` hit `GET /stations`, which reads from PostgreSQL. `/search` is left out on purpose: it would measure Deezer rather than this app, and flooding a third-party API is abuse.

```bash
k6 run load-test/stations.js   # expected load
k6 run load-test/stress.js     # increasing load until something gives
```

Both scripts fail if more than 1% of requests error or if p95 latency goes above 300 ms. Full outputs are in `load-test/results/`.

| | Load test | Stress test |
|---|---|---|
| Virtual users | 50, one request per second each | 50 → 100 → 200, no pause between requests |
| Duration | 2 min | 2 min |
| Requests | 4,522 | 813,193 |
| Throughput | ~50 req/s at peak | ~6,800 req/s on average |
| Median latency | 2.8 ms | 7.8 ms |
| p95 latency | 4.9 ms | 41 ms |
| Errors | 0 | 0 |
| App CPU (peak) | ~54% of one core | |

What the numbers say:

- At the expected load the service has plenty of headroom: p95 stays under 5 ms and the app uses about half a CPU core.
- Under stress the app kept serving without a single error up to 200 concurrent users. Latency grows with concurrency (p95 from 5 ms to 41 ms) because requests start queuing for CPU and for the 10 connections in the database pool.
- The first run after startup is slower (p95 11.9 ms on a cold JVM against 4.9 ms warm), which matters when new instances start under traffic.

Setup and caveats: MacBook Air (Apple Silicon, 8 cores) with Docker Desktop. k6 runs on the same laptop and competes with the app for CPU, so these figures are a lower bound, not a capacity claim. `/stations` returns a handful of rows; heavier queries would cost more.

## Structure

```
station/    Station entity, repository, service, controller, request DTO, seeder
music/      Deezer client, search endpoint, response DTOs
error/      Global exception handler
security/   API key filter
load-test/  k6 scripts and saved results
```

A request goes controller, service, repository. Controllers deal with HTTP only, services hold the rules, repositories talk to the database. The frontend never calls Deezer directly: it calls `/search`, and `DeezerClient` is the only class that knows the provider exists.

## Security decisions

- **Secrets stay out of the repository.** The API key and database password come from environment variables with no default value, so a missing secret fails at startup instead of leaving the service open. `.dockerignore` keeps `.env` out of the image.
- **Write endpoints require an API key.** A servlet filter rejects unauthenticated `POST` and `DELETE` requests before they reach a controller. Reads stay public.
- **Constant-time key comparison.** The filter uses `MessageDigest.isEqual`, so response time does not reveal how many characters of a guessed key were correct.
- **The API key is never in the frontend code.** The page asks for it and keeps it in `sessionStorage` for the current tab only.
- **Clients never receive entities.** `POST /stations` accepts a request DTO with only `name` and `description`, both validated, so a caller cannot set fields such as `id`.
- **Errors do not leak internals.** One handler turns every exception into a Problem Details response. Unexpected errors return a generic 500 and the real cause goes to the server log only.
- **Upstream failures are contained.** Calls to Deezer have timeouts, and any failure becomes a 502 instead of a hung request or a stack trace.
- **No HTML injection from provider data.** The frontend renders track titles with `textContent`, never `innerHTML`.
- **Containers run with least privilege.** The app runs as a non-root user in a JRE-only image. PostgreSQL has no published port, so only the app can reach it, and the metrics port is bound to localhost.

## Known limitations

- A single shared API key, with no users or roles. The next step is moving authentication to Spring Security.
- No rate limiting, and no maximum length on the search query.
- The schema is created by Hibernate (`ddl-auto=update`). A real deployment would use versioned migrations with Flyway or Liquibase.
- Load tests run on the same machine as the service. A fair capacity number needs the load generator on separate hardware.
- Stations do not hold their own tracks yet. Selecting a station searches by its name.
- The level meters are a decorative animation, not a reading of the audio.

## License

MIT. See [LICENSE](LICENSE).