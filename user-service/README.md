# user-service

Stores local user records and synchronizes user data from Keycloak.

## What it does

- Persists user profiles in `user_accounts` table.
- Pulls users from Keycloak Admin API.
- Upserts users by Keycloak ID.
- Marks missing users as inactive when no longer in Keycloak.
- Supports both scheduled sync and manual sync endpoint.

## Configuration

Set in `src/main/resources/application.properties`:

- `keycloak.sync.enabled` - enable periodic sync.
- `keycloak.sync.cron` - cron expression for scheduler.
- `keycloak.sync.server-url` - Keycloak base URL.
- `keycloak.sync.realm` - realm to sync.
- `keycloak.sync.client-id` - admin client id.
- `keycloak.sync.client-secret` - admin client secret.
- `keycloak.sync.page-size` - page size for user fetch.

## API

- `GET /api/users?activeOnly=true`
- `GET /api/users/{keycloakId}`
- `POST /api/users/sync`

## Run

```powershell
cd D:\work\avira-app\user-service
mvn spring-boot:run
```

## Test

```powershell
cd D:\work\avira-app\user-service
mvn test
```

