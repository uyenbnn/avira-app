# AGENTS Guide for `avira-app`

## Repo Shape and Service Boundaries
- Maven multi-module root (`pom.xml`) with 5 modules: `common-lib`, `user-service`, `authentication-service`, `application-initialization-service`, `project-service`.
- `authentication-service` is stateless identity API (no DB autoconfig; see `AuthenticationServiceApplication`). It owns auth/token flows and Keycloak role updates.
- `user-service` owns domain profile data (JPA + PostgreSQL) and JWT-protected user/profile endpoints.
- `application-initialization-service` owns bootstrap/seed lifecycle for Keycloak and messaging (`POST /init`, `POST /init/keycloak`, `POST /init/messaging`, alias `POST /init/streams`, plus optional startup autorun).
- `common-lib` is the shared contract layer (API path constants, migration headers, role constants, WebClient wrappers, messaging abstractions, event topics/actions).
- `project-service` manages multi-tenant configurations (JPA + PostgreSQL) and the full application/product lifecycle beneath each tenant. Event-driven: publishes `TenantCreatedEvent` for application-initialization-service bootstrap and `ApplicationCreatedEvent` on `EventTopics.APPLICATION_DOMAIN`. Also **consumes** `UserRegisteredEvent` to auto-create a default tenant per new user.

## Current Architecture (Important Historical Context)
- Read `CHANGELOG.md` first: this repo is mid-migration from legacy user-service auth flows to dedicated `authentication-service`.
- Practical result: treat `authentication-service` as the source of truth for identity operations; avoid reintroducing auth logic into `user-service`.
- Keycloak bootstrap logic exists in both `user-service` (`KeycloakRealmBootstrapService`) and `application-initialization-service` (`KeycloakInitializationService`); newer phases move ownership to initialization service.
- Registration is now split across services: `authentication-service` provisions the Keycloak identity (`KeycloakUserRegistrationService`) and publishes `UserRegisteredEvent`, while `user-service` materializes the local record from that event (`UserRegisteredEventConsumer`, `UserService#createFromRegisteredEvent`).
- Tenancy bootstrap is event-driven: `project-service` creates tenant records and publishes `TenantCreatedEvent` to `EventTopics.TENANT_DOMAIN` for `application-initialization-service` consumption (for Keycloak realm/client per-tenant setup in future phases).
- **1 user → 1 tenant (enforced):** `TenantService.create()` guards with `findByOwnerId` and throws `ConflictException` if the owner already has a tenant. `TenantService.createDefaultTenantForUser(UserRegisteredEvent)` auto-creates an `{username}-workspace` tenant when a user registers (idempotent — skips if tenant already exists).
- **1 tenant → many applications:** `Application` entity lives under `project-service/application/` with `tenantId` FK to `tenants`. Each application has a `kind` (`PERSONAL_WEB_APP`, `TOOLBOX_WEBAPP`, `ECOMMERCE_APP`), optional custom `domain`, and a generated sub-domain (`{app-name}.{username}.avira.io`) when no domain is supplied.

## API/Path Conventions Specific to This Repo
- `authentication-service`, `user-service`, `project-service`, and `application-initialization-service` all set `server.servlet.context-path=/api`; controller mappings are relative to `/api` externally.
- Auth endpoints are implemented in `authentication-service` at controller-local `/auth/*` (`AuthenticationController`), so the external paths are `/api/auth/*`; this includes `PUT /api/auth/users/{userId}/roles` for Keycloak realm-role updates.
- Shared auth constants exist in `common-lib` (`AuthApiPaths`, `AuthMigrationHeaders`); prefer these over new hardcoded strings.
- User endpoints use `/api/users` (`UserController`, `UserProfileController`) and `UserApiPaths.USERS_BASE` in security config.
- Tenant endpoints use `/api/tenants` (`TenantController`). `GET /api/tenants` (list all) is **ADMIN-only** via `@PreAuthorize("hasRole('ADMIN')")`.
- Application endpoints are nested: `/api/tenants/{tenantId}/applications` (`ApplicationController`). Admin can list all applications across all tenants via `GET /api/applications` (`@PreAuthorize("hasRole('ADMIN')")`).
- Initialization endpoints are controller-local `/init`, `/init/keycloak`, `/init/messaging` (plus `/init/streams` alias), so the external paths are `/api/init*`.
- Keycloak roles are centralized in `common-lib` `UserRoles` (`USER`, `ADMIN`, `SELLER`, `BUYER`, `ANONYMOUS`, plus `UserRoles.ALL`).

## Integration Points You Must Preserve
- Keycloak Admin API is used directly by:
  - `authentication-service` (`KeycloakUserRegistrationService`, `KeycloakUserRoleService`)
  - `application-initialization-service` (`KeycloakInitializationService`)
  - `user-service` (`KeycloakAdminClient`, legacy/bootstrap operations)
- Token login/refresh go through `KeycloakTokenWebClient` (`common-lib`) using form-encoded grant requests.
- Both `user-service` and `project-service` map Keycloak `realm_access.roles` to Spring authorities via `SecurityConfig#jwtAuthenticationConverter`; role naming must stay aligned.
- The cross-service registration flow depends on messaging: `authentication-service` publishes `UserRegisteredEvent` through `EventPublisher` on `EventTopics.USER_DOMAIN` / `UserDomainActions.REGISTERED`. Both `user-service` and `project-service` consume it via their own `UserDomainEventStreamListener` + `UserRegisteredEventConsumer`.
- The cross-service tenant bootstrap flow depends on messaging: `project-service` publishes `TenantCreatedEvent` through `EventPublisher` on `EventTopics.TENANT_DOMAIN` / `TenantDomainActions.CREATED`, and `application-initialization-service` will consume it for per-tenant Keycloak realm/client setup (future phases).
- `project-service` publishes `ApplicationCreatedEvent` on `EventTopics.APPLICATION_DOMAIN` / `ApplicationDomainActions.CREATED` when a new application is created.
- `application-initialization-service` messaging bootstrap manages stream/topic creation through `TopicManager` for `EventTopics.USER_DOMAIN`, `EventTopics.TENANT_DOMAIN`, and `EventTopics.APPLICATION_DOMAIN`; when adding a new domain event stream, update `common-lib` constants and initialization bootstrap together.

## Local Dev Workflow (Verified + Discoverable)
- Infra: `docker-compose.yml` starts Postgres (5455), RabbitMQ (5672/15672/5552), Keycloak (8080).
- Build all modules from root:
```bash
mvn -f "D:\work\avira-app\pom.xml" clean install
```
- Fast module test loop (verified in this workspace):
```bash
mvn -f "D:\work\avira-app\pom.xml" -pl authentication-service -am test
mvn -f "D:\work\avira-app\pom.xml" -pl project-service -am test
```
- Run modules individually with wrappers from each module folder (`mvnw.cmd spring-boot:run`).
- Postman collection for project-service: `project-service/project-service.postman_collection.json`.

## Gotchas That Frequently Break Changes
- Port overlap in defaults: `user-service`, `project-service`, and `application-initialization-service` may share port `10000`; override in local runs (`project-service` uses `10004` by default).
- `authentication-service` expects `keycloak.auth.realm=avira` and token URL defaults to `http://localhost:8080/realms/avira/...`; bootstrap realm/client first.
- Input role updates in auth are validated strictly against `UserRoles.ALL` (`KeycloakUserRoleService`), so new managed roles require updating `common-lib` first.
- `user-service` method security uses `@userAuthorization.canAccessUser(...)`; preserve this bean name when refactoring.
- Remember the `/api` servlet context path when tracing routes; `@RequestMapping("/auth")` and `@RequestMapping("/init")` and `@RequestMapping("/tenants")` are not the full external URLs.
- `application-initialization-service` auto-run now executes both Keycloak and messaging bootstrap through `InitializationOrchestrationService`; use `/api/init/keycloak` or `/api/init/messaging` when you need to retry only one side.
- `project-service` requires `@EnableConfigurationProperties({ApplicationProperties.class})` on `ProjectServiceApplication` to inject `spring.application.name` into the RabbitMQ Stream consumer name.
- `project-service` messaging is disabled by default (`avira.messaging.enabled=false` unless `AVIRA_MESSAGING_ENABLED=true`). The `UserDomainEventStreamListener` requires `avira.messaging.enabled=true` and `avira.messaging.provider=rabbitmq-stream` to activate.
- `ForbiddenException` is now in `common-lib` and handled by `CommonControllerAdvice` (→ HTTP 403). Use it in service layer ownership checks instead of `ResponseStatusException`.
- When adding a consumer for an existing stream (e.g., adding `project-service` consuming `user-domain`), the stream must already exist (created by `application-initialization-service`). Start `application-initialization-service` first and call `POST /api/init/messaging`.

## Test Patterns in This Codebase
- Unit tests are JUnit 5 + Mockito, mostly direct service/controller tests (see `authentication-service/src/test/java/...`).
- Bootstrap services are tested via mocked Keycloak resources and `ReflectionTestUtils` property injection.
- Messaging/bootstrap changes are covered with focused unit tests too: see `application-initialization-service` controller/orchestration tests and `MessagingInitializationServiceTest` with mocked `TopicManager`.
- Event-driven registration is unit-tested by verifying `EventPublisher.publish(...)` in `authentication-service` (`KeycloakUserRegistrationServiceTest`); follow that pattern for new domain events.
- Tenant CRUD, event publishing, 1-tenant-per-user guard, and default tenant auto-creation are unit-tested via `TenantServiceTest` with mocked repository and `EventPublisher`; see `project-service` for the pattern.
- Application CRUD, ownership guard (`ForbiddenException`), domain conflict, and event publishing are unit-tested via `ApplicationServiceTest` in `project-service`; follow that pattern for any new sub-resource.
- For behavior changes, add focused tests near touched service/controller rather than broad integration-only coverage.
