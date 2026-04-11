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
