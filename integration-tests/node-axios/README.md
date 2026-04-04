# Node Axios Integration Tests

This folder provides end-to-end flow checks for Avira services:

1. initialize Keycloak + messaging (optional)
2. register user
3. login user
4. create tenant (or resolve default tenant created by event)
5. create applications (all three kinds)
6. admin lists all applications (and users if `USER_BASE_URL` is set)

## Prerequisites

- Node.js 18+
- Running Avira services (local modules or K3s + port-forward)

## Setup

```bash
cd D:/work/avira-app/integration-tests/node-axios
npm ci
```

Copy env template if needed:

```bash
copy .env.example .env.docker
```

## Run

Docker/local ports:

```bash
npm test
```

K3s port-forward profile:

```bash
npm run test:k3s
```

User-service authorization + CRUD scenario (admin list users, self-only access, admin CRUD):

```bash
npm run test:user-service
```


## Notes

- `POST /api/tenants` can return `409` when the default tenant was already auto-created from `UserRegisteredEvent`; test then resolves existing tenant by owner.
- The script generates unique usernames per run to keep tests repeatable.
- `test:user-service` expects `USER_BASE_URL` and valid admin credentials (`ADMIN_EMAIL`, `ADMIN_PASSWORD`) in `.env.user-local`.

