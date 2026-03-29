package com.avira.commonlib.constants;

import java.util.Set;

public final class UserRoles {

    public static final String USER = "USER";
    public static final String ADMIN = "ADMIN";
    public static final String SELLER = "SELLER";
    public static final String BUYER = "BUYER";
    public static final String ANONYMOUS = "ANONYMOUS";

    /** All managed roles — used for input validation and scoped role sync. */
    public static final Set<String> ALL = Set.of(USER, ADMIN, SELLER, BUYER, ANONYMOUS);

    private UserRoles() {
    }
}

