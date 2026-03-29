package com.avira.commonlib.constants;

public final class AuthMigrationHeaders {

    public static final String WARNING_HEADER = "Warning";
    public static final String WARNING_VALUE =
            "299 - Deprecated endpoint. Use authentication-service " + AuthApiPaths.AUTH_BASE;

    public static final String MIGRATION_HEADER = "X-Auth-Migration";
    public static final String MIGRATION_VALUE = "Use authentication-service";

    private AuthMigrationHeaders() {
    }
}

