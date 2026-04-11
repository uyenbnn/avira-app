Purpose:
- Saas Platform can create a Saas application for quick start up
- feature unlock based on subscription

who use this platform:
- PLATFORM ADMIN: owner of this application
- BUSINESS USER: USER of this platform
- END USER: user of BUSINESS ADMIN

Feature:
    - PLATFORM ADMIN:
        - Monitor platform
        - all permission
        - monitor all user

    - BUSINESS USER:
        - Authenticate, register, login, logout.
        - Auto create tenant
        - CRUD Application
        - Setup application (example: domain, application configuration, enable authenticate)
        - 3 kind of application login (No Auth, use SHARED realm, use dedicated realm)
        - Their users can authenticate to their apps
        - user can navigate to their app use their provided domain

    - END USER:
        - Can use application of BUSINESS USER