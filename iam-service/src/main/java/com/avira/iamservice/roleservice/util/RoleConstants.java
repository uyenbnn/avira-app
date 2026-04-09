package com.avira.iamservice.roleservice.util;

import java.util.Set;

public final class RoleConstants {

    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final Set<String> SUPPORTED = Set.of(ADMIN, USER);

    private RoleConstants() {
    }
}
