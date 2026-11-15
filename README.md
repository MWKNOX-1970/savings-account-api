# Savings Account API

A small Spring Boot 3 (Java 17) API for creating and fetching savings accounts.
It uses Postgres, caches GETs in Redis, and builds with Maven.

## Run

You need a Postgres database. Redis is optional.

**1. Postgres.** Any Postgres 14+ will do. Create an empty database and user, and Flyway sets up the
tables when the app starts:

```sql
CREATE USER bank WITH PASSWORD 'bank';
CREATE DATABASE bank OWNER bank;
```

**2. Redis (optional).** If you've got it running on the default port, just start the app:

```bash
mvn spring-boot:run
```

If not, switch the cache off:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--app.cache.enabled=false
```

The defaults are `jdbc:postgresql://localhost:5432/bank` with user `bank` and password `bank`, and Redis on
`localhost:6379`. To change them, set `DB_URL`, `DB_USER`, `DB_PASSWORD`, `REDIS_HOST` or `REDIS_PORT`.

Got Docker? `docker compose up -d` starts Postgres and Redis with those defaults, but you don't need it.

## Test

```bash
mvn test
```

Most tests need nothing else. `AccountApiIntegrationTest` runs against a real Postgres using
Testcontainers, so it needs Docker. If Docker isn't running it skips itself and the build still passes.

## API

### `POST /api/v1/accounts`

```bash
curl -i -X POST localhost:8080/api/v1/accounts \
  -H 'Content-Type: application/json' \
  -d '{"customerName":"Tommy Smith","accountNickName":"New PC"}'
```

You get a `201 Created` with a `Location` header and the new account. The nick name is optional.

```json
{ "id": 1, "accountNumber": "483920174652", "customerName": "Jane Doe", "accountNickName": "Holiday fund" }
```

| Status | When |
|---|---|
| 400 | `customerName` is missing or blank, the nick name isn't 5 to 30 characters or is only spaces, or the JSON is malformed |
| 409 | the customer already has 5 accounts |
| 422 | the nick name is on the offensive list |
| 503 | the database is down or timed out |

### `GET /api/v1/accounts/{accountNumber}`

Returns the account. You get a `400` if the number isn't 12 digits, a `404` if there's no such account, and a
`503` if the database is down and the account isn't in the cache.

Every error comes back in the same shape:
`{ "status": 400, "error": "Bad Request", "message": "...", "details": ["..."] }`

## Design

```
web          controller, request/response records, AccountResponseConverter, exception handler
action       CreateAccountAction, GetAccountAction
model        Account (a plain record the actions return)
persistence  entities, repositories, AccountEntityConverter, AccountReader (cached reads)
support      OffensiveNickNameChecker, AccountNumberGenerator
```

- **Layers:** the controller pulls plain values out of the request and passes them to the action. The action
  hands back an `Account`, and the controller converts it to an `AccountResponse` using
  `AccountResponseConverter`. Request and response objects never go into or come out of an action, and the
  database entity stays on the persistence side (`AccountEntityConverter` turns it into an `Account`).
- **Offensive nick names** are stored in the `offensive_nicknames` table, so you can change them without
  redeploying. The checker holds them in memory and reloads every 5 minutes
  (`app.offensive-nicknames.refresh-minutes`). It ignores case and punctuation, and it maps look-alike
  characters, so `b4dw0rd` is treated as `badword`.
- **Caching:** `AccountReader` caches accounts by account number for 10 minutes. If Redis is down the error is
  logged and the request just reads from Postgres.
- **Database outages:** the connection timeout is 3 seconds, and connection or transient database errors
  return a `503`.
- **Account numbers** are 12 random digits. They're checked against existing accounts before saving, and a
  unique constraint on the column is the backstop.
- Account details live in one table (`savings_accounts`). The offensive words are in a separate lookup table.

## TODOs - not any particular order

- **API docs:** add an OpenAPI spec for the endpoints, for example generated from the code with springdoc,
  so the docs can't drift from the controller.
- **Concurrency:** two requests at the same moment could pick the same account number, or both slip past
  the 5-account limit. Retry the create if the unique constraint fails, and lock on a customer id.
- **Customer identity:** customers are matched by name only, because there's no customer table/entity linked to the accounts.
- **Offensive word list:** swap the placeholder list for a maintained one or a moderation service. Matching
  on substrings also gives false positives.
- **Performance:** the word check goes through every word on each create, which won't scale to a big list.
- **Multiple languages:** add a language column to the word list, check the right language for each request,
  and make the normalising language specific (accents, non-Latin scripts).
- **Standalone service:** if other services need the offensive check, move it into its own service. Make
  `OffensiveNickNameChecker` an interface, add an implementation that calls the service over HTTP, and decide
  what happens when it's down (reject the account, or let it through and review later).
- **Security:** the API is open to anyone. Add authentication and authorization, so a customer can only
  create and see their own accounts. Also add HTTPS, rate limiting (to slow down account-number guessing),
  and checks on who can read an account by its number.
- Metrics and tracing, API docs.- A proper account number format with a branch code and check digit.
- A Redis-backed integration test, and cache clearing once accounts can be changed or closed.
- **Logging:** there's very little logging at the moment (just database errors). Add structured logs for
    each request and for key events like account created or rejected, with a request/correlation id, and take
    care not to log customer details.
- **Exposed id:** the response returns the database's sequential id. Obfuscate it or use a separate public
    id (for example a UUID) so it can't be guessed or used to estimate the number of accounts.
- **Duplicate nick names:** a customer can give two accounts the same nick name, which makes them hard to
    tell apart. Decide whether to reject it, and if so check (case-insensitively) against that customer's
    existing accounts, ideally backed by a unique index on customer and nick name.