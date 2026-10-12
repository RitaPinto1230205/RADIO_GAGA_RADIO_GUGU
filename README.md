# Radio Gaga

A small radio studio in the browser, backed by a Spring Boot REST API. You create stations, search for music through the Deezer API and play 30-second previews on a turntable.

I built it to practise backend fundamentals in Java: REST design, error handling, configuration through environment variables and basic API security.

![Radio Gaga playing a track](docs/Captura%20de%20ecrã%202026-10-12,%20às%2000.37.42.png)

## Stack

- Java 21, Spring Boot 4 (Web, Validation, Data JPA)
- H2 in-memory database
- Deezer public API for search and previews
- Plain HTML, CSS and JavaScript for the frontend, served by Spring Boot

## Run it locally

You need Java 21 or newer.

```bash
git clone git@github.com:RitaPinto1230205/RADIO_GAGA_RADIO_GUGU.git
cd RADIO_GAGA_RADIO_GUGU

# create your own API key for the write endpoints
echo "RADIOGAGA_API_KEY=$(openssl rand -hex 32)" > .env

./mvnw spring-boot:run
```

Then open http://localhost:8080.

The database lives in memory, so stations are lost when the app stops.

## Configuration

| Variable | Required | Default | Purpose |
|---|---|---|---|
| `RADIOGAGA_API_KEY` | yes | none | Key required by the write endpoints. The app refuses to start without it. |
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

## Structure

```
station/    Station entity, repository, service, controller, request DTO
music/      Deezer client, search endpoint, response DTOs
error/      Global exception handler
security/   API key filter
```

A request goes controller, service, repository. Controllers deal with HTTP only, services hold the rules, repositories talk to the database. The frontend never calls Deezer directly: it calls `/search`, and `DeezerClient` is the only class that knows the provider exists.

## Security decisions

- **Secrets stay out of the repository.** The API key comes from an environment variable with no default value, so a missing key fails at startup instead of leaving the API open.
- **Write endpoints require an API key.** A servlet filter rejects unauthenticated `POST` and `DELETE` requests before they reach a controller. Reads stay public.
- **Constant-time key comparison.** The filter uses `MessageDigest.isEqual`, so response time does not reveal how many characters of a guessed key were correct.
- **Clients never receive entities.** `POST /stations` accepts a request DTO with only `name` and `description`, both validated, so a caller cannot set fields such as `id`.
- **Errors do not leak internals.** One handler turns every exception into a Problem Details response. Unexpected errors return a generic 500 and the real cause goes to the server log only.
- **Upstream failures are contained.** Calls to Deezer have timeouts, and any failure becomes a 502 instead of a hung request or a stack trace.
- **No HTML injection from provider data.** The frontend renders track titles with `textContent`, never `innerHTML`.

## Tests

```bash
./mvnw test
```

Integration tests start the full application and check the API key rules (401 without or with a wrong key, 201 with a valid one, public reads), input validation and the 404 response.

## Known limitations

- A single shared API key, with no users or roles. The next step is moving authentication to Spring Security.
- No rate limiting, and no maximum length on the search query.
- H2 in memory. A persistent database such as PostgreSQL is needed before any deployment, and the H2 console must stay disabled outside local development.
- Stations do not hold their own tracks yet. Selecting a station searches by its name.
- The level meters are a decorative animation, not a reading of the audio.

## License

MIT. See [LICENSE](LICENSE).