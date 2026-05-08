# ADR 003: HR Cache Invalidation Protocol

## Context and Problem Statement

The `mail-service` caches employee data from the external HR service to improve performance and reduce network load. Specifically, the `hrEmployee` cache stores `EmployeeDto` objects with a TTL of 60 minutes.

When an employee's data (such as position or status) changes in the HR service, the `mail-service` needs to be notified to invalidate its local cache. Failure to do so may lead to inconsistent behavior, such as users retaining permissions or positions that have already been revoked or changed in the HR service.

## Decision Drivers

*   **Simplicity**: The solution should be easy to implement and maintain.
*   **Latency**: Cache invalidation should happen as soon as possible after the change.
*   **Reliability**: The notification should be delivered reliably.
*   **Infrastructure**: Leverage existing infrastructure (Redis, HTTP) before introducing new components (Kafka).

## Considered Options

1.  **Webhook (HTTP POST)**: HR service calls an endpoint in `mail-service`.
2.  **Redis Pub/Sub**: HR service publishes a message to a Redis channel; `mail-service` listens.
3.  **Kafka / Message Queue**: HR service publishes an event to a topic; `mail-service` consumes.

## Decision Outcome

Chosen option: **Option 1: Webhook (HTTP POST)**, with an internal event-driven architecture to allow easy addition of Option 2 if needed.

### Rationale

*   **Directness**: Webhooks are the industry standard for simple service-to-service notifications.
*   **No New Infrastructure**: We already use HTTP/REST. Redis Pub/Sub is also a good candidate but requires the HR service to have access to the same Redis instance or for a bridge to be built.
*   **Testability**: Endpoint-based invalidation is easier to test and trigger manually if needed.
*   **Security**: Can be secured via internal network restrictions, API Keys, or HMAC signatures.

## Implementation Details

### Endpoint
`POST /api/internal/hr/cache/invalidate`

### Request Body
```json
{
  "type": "EMPLOYEE",
  "id": "123",
  "action": "UPDATE"
}
```

### Internal Flow
1. `HrCacheController` receives the request.
2. `HrCacheController` publishes a Spring `HrCacheInvalidationEvent`.
3. `HrCacheInvalidationListener` handles the event and calls `cacheManager.getCache("hrEmployee").evict("emp:" + id)`.

## Security

The endpoint should be restricted to:
1.  Internal network CIDR (if possible).
2.  Authenticated via a shared secret (API Key) in the header `X-Internal-Token`.
3.  Permitted in `SecurityConfig.java` for the specific path.
