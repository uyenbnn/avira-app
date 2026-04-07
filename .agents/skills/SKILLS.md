convention:
- RESTful API design principles for endpoint naming and structure.
- Use of HTTP status codes to indicate the outcome of API requests.
- Clear separation of concerns between different layers of the application (e.g., controllers, services, repositories).
- Use of DTOs (Data Transfer Objects) for data exchange between layers and services to decouple internal models from external representations.
- Implementation of error handling and logging mechanisms to facilitate debugging and monitoring of the application.
- Rest client api path is not store in application properties.
- all rest client api path is store in common-lib as constant, and we can use it across the application to avoid hardcoding the api path in multiple places and make it easier to maintain and update the api paths in the future if needed.

Coding styles:
- Simple and consistent naming conventions for variables, methods, and classes.
- Use of meaningful and descriptive names for functions and variables to enhance code readability.
- Adherence to SOLID principles for object-oriented design to promote maintainability and scalability.
- Simple as much as possible, but no simpler. Avoid over-engineering and unnecessary complexity in the codebase.
- Use of design patterns where appropriate to solve common problems and improve code organization.
- Consistent formatting and indentation to enhance readability and maintain a clean codebase.
