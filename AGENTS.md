# AGENTS Guide for `avira-app`

## Repo Shape and Service Boundaries
- Maven multi-module root (`pom.xml`) with 4 modules: `common-lib`, `user-service`, `authentication-service`, `application-initialization-service`.
- `authentication-service` is stateless identity API (no DB autoconfig; see `AuthenticationServiceApplication`). It owns auth/token flows and Keycloak role updates.
- `user-service` owns domain profile data (JPA + PostgreSQL) and JWT-protected user/profile endpoints.
- `application-initialization-service` owns Keycloak bootstrap/seed lifecycle (`POST /init`, plus optional startup autorun).
- `common-lib` is the shared contract layer (API path constants, migration headers, role constants, WebClient wrappers).

## Current Architecture (Important Historical Context)
- Read `CHANGELOG.md` first: this repo is mid-migration from legacy user-service auth flows to dedicated `authentication-service`.
- Practical result: treat `authentication-service` as the source of truth for identity operations; avoid reintroducing auth logic into `user-service`.
- Keycloak bootstrap logic exists in both `user-service` (`KeycloakRealmBootstrapService`) and `application-initialization-service` (`KeycloakInitializationService`); newer phases move ownership to initialization service.

## API/Path Conventions Specific to This Repo
- Auth endpoints are implemented in `authentication-service` at `/auth/*` (`AuthenticationController`).
- Shared auth constants exist in `common-lib` (`AuthApiPaths`, `AuthMigrationHeaders`); prefer these over new hardcoded strings.
- User endpoints use `/api/v1/users` (`UserController`, `UserProfileController`) and `UserApiPaths.USERS_BASE` in security config.
- Keycloak roles are centralized in `common-lib` `UserRoles` (`USER`, `ADMIN`, `SELLER`, `BUYER`, `ANONYMOUS`, plus `UserRoles.ALL`).

## Integration Points You Must Preserve
- Keycloak Admin API is used directly by:
  - `authentication-service` (`KeycloakUserRegistrationService`, `KeycloakUserRoleService`)
  - `application-initialization-service` (`KeycloakInitializationService`)
  - `user-service` (`KeycloakAdminClient`, legacy/bootstrap operations)
- Token login/refresh go through `KeycloakTokenWebClient` (`common-lib`) using form-encoded grant requests.
- User-service security maps Keycloak `realm_access.roles` to Spring authorities (`SecurityConfig#jwtAuthenticationConverter`), so realm role naming must stay aligned.

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

## Test Patterns in This Codebase
- Unit tests are JUnit 5 + Mockito, mostly direct service/controller tests (see `authentication-service/src/test/java/...`).
- Bootstrap services are tested via mocked Keycloak resources and `ReflectionTestUtils` property injection.
- For behavior changes, add focused tests near touched service/controller rather than broad integration-only coverage.
