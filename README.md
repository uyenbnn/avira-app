Application Overview
----------------
This application is designed to manage user authentication and profiles for an e-commerce platform. It consists of three main modules: Application Initialization Service, Authentication Service, and User Service. Each module has specific responsibilities and endpoints to handle various aspects of user management and authentication.   
Application name: Avira

Coding styles:
    - Simple and consistent naming conventions for variables, methods, and classes.
    - Use of meaningful and descriptive names for functions and variables to enhance code readability.
    - Adherence to SOLID principles for object-oriented design to promote maintainability and scalability.
    - Simple as much as possible, but no simpler. Avoid over-engineering and unnecessary complexity in the codebase.
    - Use of design patterns where appropriate to solve common problems and improve code organization.
    - Consistent formatting and indentation to enhance readability and maintain a clean codebase.

convention:
    - RESTful API design principles for endpoint naming and structure.
    - Use of HTTP status codes to indicate the outcome of API requests.
    - Clear separation of concerns between different layers of the application (e.g., controllers, services, repositories).
    - Use of DTOs (Data Transfer Objects) for data exchange between layers and services to decouple internal models from external representations.
    - Implementation of error handling and logging mechanisms to facilitate debugging and monitoring of the application.
    - Rest client api path is not store in application properties.
    - all rest client api path is store in common-lib as constant, and we can use it across the application to avoid hardcoding the api path in multiple places and make it easier to maintain and update the api paths in the future if needed.

technologies used:
- Spring Boot: A Java-based framework for building web applications and microservices.
- Keycloak: An open-source identity and access management solution for authentication and authorization.
- JWT (JSON Web Tokens): A compact, URL-safe means of representing claims to be transferred
- PostgreSQL: A powerful, open-source relational database management system for storing user profiles and related information.
- Docker: A platform for developing, shipping, and running applications in containers, ensuring consistency across different environments.
- Kubernetes: An open-source container orchestration platform for automating the deployment, scaling, and management of containerized applications.
- Spring Security: A powerful and customizable authentication and access control framework for Java applications, used to secure the application and manage user roles and permissions.
- Spring Data JPA: A part of the Spring Framework that provides an abstraction layer for working with relational databases, making it easier to perform CRUD operations and manage database interactions.
- Spring Web: A module of the Spring Framework that provides tools for building web applications, including RESTful APIs, which are used to expose the endpoints for authentication and user management.
- Spring Cloud: A set of tools for building distributed systems and microservices, used to manage service discovery, configuration, and communication between the different modules of the application.
- Spring Boot Actuator: A module of Spring Boot that provides production-ready features for monitoring and managing the application, such as health checks and metrics.
- Spring Boot DevTools: A module of Spring Boot that provides development-time features, such as automatic restarts and live reload, to enhance the development experience.
- Lombok: A Java library that helps reduce boilerplate code by generating getters, setters, constructors, and other common methods at compile time, improving code readability and maintainability.
- MapStruct: A code generator that simplifies the mapping between Java bean types, used to convert between entity classes and DTOs (Data Transfer Objects) in the application.
- JUnit and Mockito: Testing frameworks for writing unit tests and mocking dependencies, ensuring the reliability and correctness of the application.
- Swagger/OpenAPI: Tools for documenting and testing RESTful APIs, providing a user-friendly interface for exploring the endpoints and their functionalities.
- Flyway: A database migration tool that helps manage and version control database schema changes, ensuring consistency across different environments and simplifying the deployment process.
- Logback: A logging framework for Java applications, used to provide flexible and efficient logging capabilities for monitoring and debugging the application.
- Spring Cloud Config: A tool for externalizing configuration properties, allowing the application to manage configurations across different environments and services in a centralized manner.
- Spring Cloud Gateway: A library for building API gateways, used to route requests to the appropriate services and handle cross-cutting concerns such as authentication and rate limiting.
- Spring Cloud Sleuth: A tool for distributed tracing, used to track and analyze the flow of requests across different services in the application, helping to identify performance bottlenecks and troubleshoot issues.
- Webclient for synchronize data between services, such as Keycloak for authentication and authorization, and other services in the future if needed.
- RabbitMQ: A message broker that enables asynchronous communication between services, used for decoupling components and improving scalability and resilience of the application.
- Redis: An in-memory data structure store, used for caching and improving the performance of the application by reducing the load on the database and providing fast access to frequently used data.

application-initialization-service:
    description: 
        - The application initialization process involves setting up the necessary configurations, dependencies, and services required for the authentication and user services to function properly. 
        - This includes configuring database connections, initializing the web server, and setting up any required middleware or utilities.
        - Init keycloak tenant and client for authentication and authorization
        - Init admin user for keycloak to manage users and roles
    endpoints:
        - POST /init: "Initializes the application by setting up necessary configurations and dependencies. This endpoint should be called once during the initial setup of the application."

authentication-service:
    description: 
        - Authentication Service is responsible for handling user authentication and authorization. It provides endpoints for user registration, login, and token management.
        - no database
        - connect to keycloak for authentication and authorization
    endpoints:
        - POST /register: "Registers a new user with the provided credentials."
        - POST /login: "Authenticates a user and returns a JWT token for authorized access."
        - POST /refresh-token: "Refreshes the JWT token for an authenticated user."

user-service:
    description: 
        - User Service manages user profiles and related information. 
        - It provides endpoints for retrieving and updating user details.
    endpoints:
        - POST /users: "Creates a new user profile with the provided information."
        - GET /users/{id}: "Retrieves the profile information of a user by their ID."
        - PUT /users/{id}: "Updates the profile information of a user by their ID."
        - GET /users:
            - Retrieves a list of all users in the system.
            - filter by user type like seller or buyer, shop ower or customer, and other user types that we will have in the future.

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
