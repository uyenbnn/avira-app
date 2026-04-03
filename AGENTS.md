# AGENTS Guide for `avira-app`

## Repo Shape and Service Boundaries
- Maven multi-module root (`pom.xml`) with 5 modules: `common-lib`, `user-service`, `authentication-service`, `application-initialization-service`, `project-service`.
- `authentication-service` is stateless identity API (no DB autoconfig; see `AuthenticationServiceApplication`). It owns auth/token flows and Keycloak role updates.
- `user-service` owns domain profile data (JPA + PostgreSQL) and JWT-protected user/profile endpoints.
- `application-initialization-service` owns bootstrap/seed lifecycle for Keycloak and messaging (`POST /init`, `POST /init/keycloak`, `POST /init/messaging`, alias `POST /init/streams`, plus optional startup autorun).
- `common-lib` is the shared contract layer (API path constants, migration headers, role constants, WebClient wrappers, messaging abstractions).
- `project-service` is present in the reactor but is currently just a Spring Boot scaffold (`ProjectServiceApplication` + `application.properties`); do not assume live controllers, schema, or integrations there yet.

## Current Architecture (Important Historical Context)
- Read `CHANGELOG.md` first: this repo is mid-migration from legacy user-service auth flows to dedicated `authentication-service`.
- Practical result: treat `authentication-service` as the source of truth for identity operations; avoid reintroducing auth logic into `user-service`.
- Keycloak bootstrap logic exists in both `user-service` (`KeycloakRealmBootstrapService`) and `application-initialization-service` (`KeycloakInitializationService`); newer phases move ownership to initialization service.
- Registration is now split across services: `authentication-service` provisions the Keycloak identity (`KeycloakUserRegistrationService`) and publishes `UserRegisteredEvent`, while `user-service` materializes the local record from that event (`UserRegisteredEventConsumer`, `UserService#createFromRegisteredEvent`).
- `user-service` still contains legacy `/api/v1/auth/register` and `/api/v1/auth/login` endpoints (`userservice.controller.AuthenticationController`) for compatibility; refresh-token ownership no longer lives there.

## API/Path Conventions Specific to This Repo
- `authentication-service`, `user-service`, and `application-initialization-service` all set `server.servlet.context-path=/api`; controller mappings are relative to `/api` externally.
- Auth endpoints are implemented in `authentication-service` at controller-local `/auth/*` (`AuthenticationController`), so the external paths are `/api/auth/*`; this includes `PUT /api/auth/users/{userId}/roles` for Keycloak realm-role updates.
- Shared auth constants exist in `common-lib` (`AuthApiPaths`, `AuthMigrationHeaders`); prefer these over new hardcoded strings.
- User endpoints use `/api/v1/users` (`UserController`, `UserProfileController`) and `UserApiPaths.USERS_BASE` in security config.
- Initialization endpoints are controller-local `/init`, `/init/keycloak`, `/init/messaging` (plus `/init/streams` alias), so the external paths are `/api/init*`.
- Keycloak roles are centralized in `common-lib` `UserRoles` (`USER`, `ADMIN`, `SELLER`, `BUYER`, `ANONYMOUS`, plus `UserRoles.ALL`).

## Integration Points You Must Preserve
- Keycloak Admin API is used directly by:
  - `authentication-service` (`KeycloakUserRegistrationService`, `KeycloakUserRoleService`)
  - `application-initialization-service` (`KeycloakInitializationService`)
  - `user-service` (`KeycloakAdminClient`, legacy/bootstrap operations)
- Token login/refresh go through `KeycloakTokenWebClient` (`common-lib`) using form-encoded grant requests.
- User-service security maps Keycloak `realm_access.roles` to Spring authorities (`SecurityConfig#jwtAuthenticationConverter`), so realm role naming must stay aligned.
- The cross-service registration flow depends on messaging: `authentication-service` publishes `UserRegisteredEvent` through `EventPublisher` on `EventTopics.USER_DOMAIN` / `UserDomainActions.REGISTERED`, and `user-service` consumes it via `UserDomainEventStreamListener` + `UserRegisteredEventConsumer` to create local records.
- `application-initialization-service` messaging bootstrap manages stream/topic creation through `TopicManager` for `EventTopics.USER_DOMAIN` and `EventTopics.APPLICATION_DOMAIN`; when adding a new domain event stream, update `common-lib` constants and initialization bootstrap together.

## Local Dev Workflow (Verified + Discoverable)
- Infra: `docker-compose.yml` starts Postgres (5455), RabbitMQ (5672/15672/5552), Keycloak (8080).
- Build all modules from root:
```bash
mvn -f "D:\work\avira-app\pom.xml" clean install
```
- Fast module test loop (verified in this workspace):
```bash
mvn -f "D:\work\avira-app\pom.xml" -pl authentication-service -am test
```
- Run modules individually with wrappers from each module folder (`mvnw.cmd spring-boot:run`).

## Gotchas That Frequently Break Changes
- Port overlap in defaults: `user-service` and `application-initialization-service` both set `server.port=10000`; override one in local runs.
- `authentication-service` expects `keycloak.auth.realm=avira` and token URL defaults to `http://localhost:8080/realms/avira/...`; bootstrap realm/client first.
- Input role updates in auth are validated strictly against `UserRoles.ALL` (`KeycloakUserRoleService`), so new managed roles require updating `common-lib` first.
- `user-service` method security uses `@userAuthorization.canAccessUser(...)`; preserve this bean name when refactoring.
- Remember the `/api` servlet context path when tracing routes; `@RequestMapping("/auth")` and `@RequestMapping("/init")` are not the full external URLs.
- `application-initialization-service` auto-run now executes both Keycloak and messaging bootstrap through `InitializationOrchestrationService`; use `/api/init/keycloak` or `/api/init/messaging` when you need to retry only one side.

## Test Patterns in This Codebase
- Unit tests are JUnit 5 + Mockito, mostly direct service/controller tests (see `authentication-service/src/test/java/...`).
- Bootstrap services are tested via mocked Keycloak resources and `ReflectionTestUtils` property injection.
- Messaging/bootstrap changes are covered with focused unit tests too: see `application-initialization-service` controller/orchestration tests and `MessagingInitializationServiceTest` with mocked `TopicManager`.
- Event-driven registration is unit-tested by verifying `EventPublisher.publish(...)` in `authentication-service` (`KeycloakUserRegistrationServiceTest`); follow that pattern for new domain events.
- For behavior changes, add focused tests near touched service/controller rather than broad integration-only coverage.
