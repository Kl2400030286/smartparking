# ParkGrid Services

ParkGrid is a Maven multi-module Spring Boot 4.1.0 application. The business behavior and
database schema remain compatible with the original consolidated API, while deployable
service boundaries are now explicit:

| Module | Application | Port | Responsibility |
|---|---|---:|---|
| `eureka` | `eureka-server` | 8761 | Service registry |
| `gateway` | `api-gateway` | 8081 | Edge routing and load balancing |
| `services/parking-service` | `parking-service` | 8080 | MySQL-backed auth, zones, slots, and compatibility API |
| `services/booking-service` | `booking-service` | 8082 | Booking facade; forwards authenticated requests to parking |
| `services/billing-service` | `billing-service` | 8083 | Billing facade; obtains quotes from parking |

Booking calls Billing for `/api/bookings/{id}/quote`, and Billing calls the parking service's
internal quote endpoint. The parking service remains the source of truth for transactions,
JWT role enforcement, overlap locking, tariffs, cancellation, and MySQL persistence. This
keeps existing clients working while allowing each service to be deployed independently.

## Build and run

The root `pom.xml` is an aggregator and pins Spring Boot `4.1.0` and the compatible Spring
Cloud Oakwood release `2025.1.2`. Run the complete build with:

```text
mvn clean verify
```

Create `parkingslot` in MySQL 8, copy `.env.example` to `.env`, and set `MYSQL_PASSWORD`
and a 32+ character `JWT_SECRET`. The application has no development fallback for either
secret and will fail fast if they are missing. Then run:

```text
docker compose up --build
```

Use `http://localhost:8081/api/...` through the gateway. Eureka registration is enabled
by default and can be disabled per service with `EUREKA_ENABLED`, `EUREKA_REGISTER`, or
`EUREKA_FETCH`. Direct service URLs are useful for diagnostics.

Existing endpoints remain under `/api/auth`, `/api/parking`, `/api/bookings`,
`/api/billing`, and `/api/admin`. Zone rates are integer cents from 5 through 100.

### First administrator

Public registration creates only `USER` accounts. For a controlled first-admin bootstrap,
set the following variables in `.env` for one startup:

```text
ADMIN_BOOTSTRAP_ENABLED=true
ADMIN_BOOTSTRAP_EMAIL=admin@example.com
ADMIN_BOOTSTRAP_PASSWORD=use-a-strong-password
```

The account is created only when that email does not already exist. Set
`ADMIN_BOOTSTRAP_ENABLED=false` after the first successful startup.

### Error responses

Validation failures return `400`, missing or invalid credentials return `401`, insufficient
roles return `403`, missing resources return `404`, and invalid booking state transitions
return `409`. Responses use a JSON `error` field so the gateway preserves a consistent API
shape.

### Tests

Run the focused service tests or the complete reactor build with:

```text
mvn -pl services/parking-service test
mvn clean verify
```
