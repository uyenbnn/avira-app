# SaaS UI App MVP

Minimal Angular MVP UI for:

- business user login and callback handling
- protected route demonstration
- tenant and application setup workflow against platform API endpoints

## Prerequisites

- Node.js 20+
- npm 10+
- Backend services reachable through gateway routes:
	- /api/iam/*
	- /api/platform/*

## Local Run

From saas-ui-app:

```bash
npm install
npm run start
```

Open http://localhost:4200.

## API Contract Mapping Used by UI

Auth flow:

- POST /api/iam/auth/login
- POST /api/iam/auth/refresh

Business workflow:

- POST /api/platform/tenants
- POST /api/platform/tenants/{tenantId}/applications
- GET /api/platform/tenants/{tenantId}/applications

## Login and Callback Notes

- Login form sends tenantId, username, password, and optional appId.
- Callback route expects query parameters:
	- accessToken
	- refreshToken
	- expiresIn (optional)

Example callback URL:

http://localhost:4200/auth/callback?accessToken=...&refreshToken=...&expiresIn=300

## Security and Config

- No secrets are stored in source.
- Access and refresh tokens are stored in browser localStorage for local MVP testing only.
- Do not commit local credentials or real tokens.

## Unit Tests

Run:

```bash
npm run test -- --watch=false
```
