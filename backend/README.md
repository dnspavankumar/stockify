# Signalist Spring Boot Backend

Standalone backend for the existing Next.js frontend.

## Build

```bash
mvn -DskipTests package
```

## Run

```bash
mvn spring-boot:run
```

The backend defaults to `http://localhost:8080`.

## Main Environment Variables

- `MONGODB_URI`
- `FINNHUB_API_KEY`
- `FRONTEND_BASE_URL`
- `SESSION_COOKIE_NAME`
- `SMTP_HOST`
- `SMTP_PORT`
- `SMTP_USERNAME`
- `SMTP_PASSWORD`
- `MAIL_ENABLED`
- `MAIL_FROM_NAME`
- `MAIL_FROM_ADDRESS`
- `DAILY_DIGEST_ENABLED`
- `DAILY_DIGEST_CRON`

## API Surface

- `POST /api/auth/sign-up`
- `POST /api/auth/sign-in`
- `POST /api/auth/sign-out`
- `GET /api/auth/session`
- `GET /api/stocks/search`
- `GET /api/market/news`
- `GET /api/watchlist`
- `GET /api/watchlist/status`
- `POST /api/watchlist`
- `DELETE /api/watchlist/{symbol}`
