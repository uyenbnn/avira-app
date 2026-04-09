package com.avira.iamservice.clientservice.util;

public final class RealmNameUtil {

    private RealmNameUtil() {
    }

    public static String defaultDedicatedRealm(String prefix, String tenantId) {
        return prefix + tenantId;
    }
}
