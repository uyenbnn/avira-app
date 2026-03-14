package com.avira.userservice.constants;

import java.util.Set;

public final class RoleConstants {

    public static final String USER = "USER";
    public static final String ADMIN = "ADMIN";
    public static final String SELLER = "SELLER";
    public static final String BUYER = "BUYER";

    public static final Set<String> BASE_ROLES = Set.of(USER, ADMIN, SELLER, BUYER);

    private RoleConstants() {
    }
}

