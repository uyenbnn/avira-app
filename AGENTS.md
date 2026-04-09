# AGENTS Guide for `avira-app`

Working flow for development:
- Update code in (*-service or *-lib)
- Run Unit tests 
- Create or update deploy in folder ./deploy
- Deploy to k3s local
- Create or update integration test using axios in integration-tests/
- Run integration tests

## 1. System Overview

This repository implements a **multi-tenant SaaS platform** where:

* Users register and create tenants (companies)
* Tenants create applications
* Each application can define its **own authentication mechanism**

Stack:

* Spring Boot microservices
* Keycloak (platform identity only)
* PostgreSQL
* Kong API Gateway
* k3s deployment

IMPORTANT:
- Update this document or create new document in ./docs as needed when new features or services are added for future reference.
- Update SKILLS.md and DOD.md when new coding conventions or definition of done criteria are introduced for future reference.
- Always follow the latest conventions and criteria defined in SKILLS.md and DOD.md when implementing new features or services.
- This document serves as a high-level architectural overview and guide for the implementation of the Avira application. It outlines the structure, responsibilities, and constraints of each service, as well as the overall design principles and patterns to be followed throughout the development process.
- The goal is to ensure a clear separation of concerns, maintainability, and scalability of the application while adhering to best practices in software development and multi-tenancy design.
- The top require for coding is easiest to understand and maintain, not the shortest code. Always prioritize readability and maintainability over clever or complex solutions.
---

## 2. Services

### 2.1 iam-service (Identity & Access Management)
structure:
- `src/main/java/com/avira/iam/init-service` (for Keycloak realm initialization and provisioning, stream init (rabbitmq stream or kafka))
- `src/main/java/com/avira/iam/authentication-service` (for platform authentication logic, e.g., Keycloak integration)
- `src/main/java/com/avira/iam/user-service` (for platform user management)
- `src/main/java/com/avira/iam/client-service` (for Keycloak client management)
- `src/main/java/com/avira/iam/role-service` (for role and permission management)
- `src/main/java/com/avira/iam/permission-service` (for data transfer objects)

Each module contains:
- `controller` (for REST endpoints)
- `service` (for business logic)
- `repository` (for database interactions)
- `dto` (for data transfer objects)
- `config` (for Keycloak and security configurations)
- `exception` (for custom exceptions)
- `mapper` (for mapping between entities and DTOs)
- `util` (for utility classes and constants)
- `integration` (for Keycloak API clients and integration logic)
- `event` (for handling events related to user and client management, support for rabbitmq stream or kafka)

Responsibilities:

* Integrate with Keycloak Admin API
* Initialize Keycloak (realm, clients, roles)
* Manage platform users
* Assign roles and permissions
* Manage Keycloak clients (public + confidential)

Key Rules:

- Default: SINGLE shared realm (avira-platform)
- Support dedicated realm per tenant (enterprise mode)
- Realm creation MUST be handled by iam-service only
- application-service MUST NOT create or manage realms

Each tenant defines identity_mode:

SHARED_REALM:
- Uses global realm
- Tenant isolation via tenant_id claim

DEDICATED_REALM:
- Own Keycloak realm
- Full identity isolation
- Supports custom IdP

iam-service MUST route all Keycloak operations based on tenant.identity_mode

Anti-Patterns
❌ Blindly creating realm per tenant
❌ application-service managing Keycloak
❌ Using realm as tenant identifier

Critical Implementation Rule:
- Introduce:
  - interface RealmResolver {
        String resolveRealm(String tenantId);
    }

Realistic Warning:
Even with hybrid:

Dedicated realm tenants are expensive
    Limit them:
     - pricing tier
     - manual approval

---

### 2.2 platform-service (Control Plane)

permission:
- Only platform admins can manage tenants and applications

structure:
- `src/main/java/com/avira/platform/tenant-service` (for tenant management)
- `src/main/java/com/avira/platform/application-service` (for application management)
- `src/main/java/com/avira/platform/configuration-service` (for application configuration management)
- `src/main/java/com/avira/platform/subscription-service` (for subscription and billing management)
- `src/main/java/com/avira/platform/ui-template-service` (for UI template management)
- `src/main/java/com/avira/platform/communication-service` (for email/SMS notifications)
- `src/main/java/com/avira/platform/audit-service` (for audit logging and monitoring)
- `src/main/java/com/avira/platform/analytics-service` (for analytics and reporting)


Each module contains:
- `controller` (for REST endpoints)
- `service` (for business logic)
- `repository` (for database interactions)
- `dto` (for data transfer objects)
- `config` (for Keycloak and security configurations)
- `exception` (for custom exceptions)
- `mapper` (for mapping between entities and DTOs)
- `util` (for utility classes and constants)
- `integration` (for Keycloak API clients and integration logic)
- `event` (for handling events related to user and client management, support for rabbitmq stream or kafka)

Responsibilities:

* Manage tenants (companies)
* Manage applications
* Store application configuration
* Define authentication mode per application

Entities:

* Tenant
* Application

Constraints:

* No authentication execution logic
* No app user management
* No runtime logic

---

### 2.3 application-service (Runtime & App Domain)

structure:
- `src/main/java/com/avira/application/administration-service` (for application administration, e.g., user management, role management)
- `src/main/java/com/avira/application/authentication-service` (for application authentication logic, e.g., strategy pattern implementations)
- `src/main/java/com/avira/application/business-logic-service` (for application-specific business logic and operations)
- `src/main/java/com/avira/application/configuration-service` (for application-specific configuration management)
- `src/main/java/com/avira/application/notification-service` (for application-specific notifications and alerts)
- `src/main/java/com/avira/application/subscription-service` (for application-specific subscription and billing management)
- `src/main/java/com/avira/application/integration-service` (for integrating with external services and APIs)

Each module contains:
- `controller` (for REST endpoints)
- `service` (for business logic)
- `repository` (for database interactions)
- `dto` (for data transfer objects)
- `config` (for Keycloak and security configurations)
- `exception` (for custom exceptions)
- `mapper` (for mapping between entities and DTOs)
- `util` (for utility classes and constants)
- `integration` (for Keycloak API clients and integration logic)
- `event` (for handling events related to user and client management, support for rabbitmq stream or kafka)

Responsibilities:

- Manage application users
- Execute application logic
- Handle application-level authentication
- Issue application JWT tokens

Constraints:

- MUST NOT use Keycloak Admin API
- MUST NOT create or manage realms
- MUST NOT manage platform users

---

common-lib:
description:
- Common module contains shared utilities, constants, and configurations used across the authentication and user services.
- It promotes code reusability and maintainability by centralizing common functionalities.
- No database
- Contains USER_ROLE constant for application, suggest for e-commerce application, we will have 3 user roles: admin, seller, and buyer. We can add more roles in the future if needed.
components:
- Utilities: "Helper functions and classes for common tasks such as error handling, logging, and data validation."
- Constants: "Shared constants used across services, such as status codes, error messages, and configuration values."
- Configurations: "Centralized configuration management for database connections, API endpoints, and other settings."
- WebClient: "A shared web client for making HTTP requests to external services, such as Keycloak for synchronize data between services."
- DTOs: "Data Transfer Objects used for defining the structure of data exchanged between services and clients."
- Exceptions: "Custom exception classes for handling specific error scenarios in a consistent manner across services."
- Middleware: "Shared middleware components for request processing, such as authentication and logging."
- Services: "Shared services that provide common functionalities, such as email notifications or caching mechanisms."
- RabbitMQ: "Shared configuration and utilities for integrating RabbitMQ for asynchronous communication between services."
- RabbitMq Stream: "Shared configuration and utilities for integrating RabbitMQ Stream for high-throughput messaging between services."


---------------

3. Identity Model
   3.1 Platform Identity (Keycloak)

Using Keycloak

Used for:

platform login
tenant ownership

Managed via iam-service only.

3.2 Application Identity
Each application has isolated users
Managed inside application-service
Stored in PostgreSQL
Independent from platform users
4. Tenant Identity Strategy (Hybrid)

Each tenant MUST define identity_mode:

SHARED_REALM
DEDICATED_REALM
4.1 SHARED_REALM (Default)
Uses global realm: avira-platform
Tenant isolation via:
tenant_id (JWT claim)
roles/groups
4.2 DEDICATED_REALM (Enterprise)
Each tenant has its own realm:
tenant_{tenantId}
Full identity isolation
Supports:
custom IdP
custom login flows
4.3 Tenant Schema
tenant
- id UUID
- name TEXT
- identity_mode TEXT
- realm_name TEXT
5. Realm Resolution (Critical)

iam-service MUST implement:

public interface RealmResolver {
String resolveRealm(String tenantId);
}

Behavior:

SHARED_REALM → return avira-platform
DEDICATED_REALM → return tenant-specific realm
6. Keycloak Provisioning

Handled ONLY by iam-service.

6.1 Shared Realm
Created once at startup
No per-tenant provisioning required
6.2 Dedicated Realm

On tenant creation:

Create realm: tenant_{tenantId}
Create clients:
public client (frontend login)
confidential client (service-to-service)
Create roles:
ADMIN
USER
7. Application Authentication Model

Each application defines its own auth strategy.

7.1 Supported auth_mode
INTERNAL
PLATFORM_SSO
EXTERNAL_OIDC
ANONYMOUS
7.2 Application Schema
application
- id UUID
- tenant_id UUID
- name TEXT
- type TEXT
- auth_mode TEXT
- auth_config JSONB
  7.3 auth_config Examples
  INTERNAL
  {
  "allowRegistration": true,
  "jwtExpirationMinutes": 60
  }
  EXTERNAL_OIDC
  {
  "issuer": "https://accounts.google.com",
  "clientId": "...",
  "clientSecret": "..."
  }
8. Authentication Execution (application-service)

MUST use strategy pattern:

public interface AuthenticationHandler {
LoginResponse authenticate(Application app, LoginRequest request);
}

Implementations:

InternalAuthenticationHandler
PlatformSsoAuthenticationHandler
OidcAuthenticationHandler
AnonymousAuthenticationHandler
9. JWT Rules
   Platform Token
   Issued by Keycloak
   MUST include tenantId (custom claim)
   Application Token
   {
   "appId": "...",
   "tenantId": "...",
   "userId": "...",
   "roles": []
   }
10. Multi-Tenancy Rules

ALL queries MUST include:

tenant_id
app_id (if applicable)
11. API Routing (Kong)
    /api/iam/* → iam-service
    /api/platform/* → platform-service
    /app/* → application-service
12. Security Rules
    NEVER trust client-provided tenant_id/app_id
    Always extract from JWT or gateway headers
    ALWAYS validate tenant ownership
13. Database Rules

Using PostgreSQL

UUID as primary key
JSONB for config
Index tenant_id, app_id
14. Keycloak Rules

Using Keycloak

Default: single shared realm (avira-platform)
Support dedicated realm per tenant
ONLY iam-service interacts with Keycloak Admin API
application-service may only validate tokens
15. Anti-Patterns
    ❌ Blindly creating realm for every tenant
    ❌ application-service managing Keycloak
    ❌ Using realm as tenant identifier
    ❌ Mixing platform and application identity
    ❌ Hardcoding authentication logic
    ❌ Skipping tenant filters in queries
16. MVP Constraints

Support:

INTERNAL
ANONYMOUS

Defer:

PLATFORM_SSO
EXTERNAL_OIDC
17. Future Extensions
    External OIDC providers (Google, Microsoft)
    Platform SSO reuse
    Tenant-provided IdP
    Plugin-based authentication system
18. Copilot Instructions
    Follow module structure strictly
    Respect service boundaries
    Always enforce tenant isolation
    Use strategy pattern for authentication
    Route Keycloak operations via iam-service only
    Generate production-ready Spring Boot code

## 13. Future Extensions

* External OIDC providers (Google, Microsoft)
* Platform SSO reuse
* Custom tenant-provided identity providers
* Plugin-based authentication marketplace

---

## 14. Copilot Instructions

When generating code:

* Always respect auth_mode
* Never assume a single authentication model
* Use strategy pattern for authentication logic
* Enforce tenant isolation in all queries
* Generate production-ready Spring Boot code
* Avoid shortcuts that break multi-tenancy

---

END OF FILE

