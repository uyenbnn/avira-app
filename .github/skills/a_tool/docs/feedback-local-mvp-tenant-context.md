# Frontend Workflow Feedback - local-mvp-tenant-context

## Scope
Implemented minimal auth-layer changes so tenant context is available to backend-required platform/application calls.

## API Contract Alignment
- Added `X-Tenant-Id` header propagation for requests targeting `/api/platform/**` and `/api/application/**` when tenant is present in auth state.
- Preserved existing `Authorization: Bearer <token>` behavior for non-login/refresh requests.

## Observed API Usability Gaps
- Tenant context currently comes from login payload and is not echoed in callback parameters; callback-based sessions may not have tenant context unless previously stored.
- Endpoint scoping is path-prefix based (`/api/platform`, `/api/application`); consider documenting this explicitly in OpenAPI or gateway contract to avoid frontend ambiguity.

## Suggested Follow-ups
- Consider returning canonical tenant context from login/refresh response to reduce client-side state coupling.
- Consider defining tenant-header requirement in OpenAPI components (header parameter) for all tenant-scoped operations.
