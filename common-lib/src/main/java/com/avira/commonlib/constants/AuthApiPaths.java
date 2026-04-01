package com.avira.commonlib.constants;

public final class AuthApiPaths {

    public static final String AUTH_BASE = "/auth";

    public static final String REGISTER = "/register";
    public static final String LOGIN = "/login";
    public static final String REFRESH_TOKEN = "/refresh-token";
    public static final String LOGOUT = "/logout";

    public static final String REGISTER_FULL = AUTH_BASE + REGISTER;
    public static final String LOGIN_FULL = AUTH_BASE + LOGIN;
    public static final String REFRESH_TOKEN_FULL = AUTH_BASE + REFRESH_TOKEN;
    public static final String LOGOUT_FULL = AUTH_BASE + LOGOUT;

    private AuthApiPaths() {
    }
}

