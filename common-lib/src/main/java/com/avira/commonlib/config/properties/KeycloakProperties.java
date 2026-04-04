package com.avira.commonlib.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private final Sync sync = new Sync();
    private final Auth auth = new Auth();
    private final Admin admin = new Admin();
    private final Realm realm = new Realm();

    public Sync getSync() {
        return sync;
    }

    public Auth getAuth() {
        return auth;
    }

    public Admin getAdmin() {
        return admin;
    }

    public Realm getRealm() {
        return realm;
    }

    public String getResolvedAuthRealm() {
        return hasText(auth.getRealm()) ? auth.getRealm() : sync.getRealm();
    }

    public String getResolvedAuthClientId() {
        return hasText(auth.getClientId()) ? auth.getClientId() : sync.getRealm() + "-user-client";
    }

    public String getResolvedAuthClientSecret() {
        return auth.getClientSecret() == null ? "" : auth.getClientSecret();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static class Sync {

        private String serverUrl = "http://localhost:8080";
        private String clientId = "admin-cli";
        private String clientSecret = "";
        private String realm = "avira";

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }
    }

    public static class Auth {

        private String realm;
        private String clientId;
        private String clientSecret = "";

        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }
    }

    public static class Admin {

        private String realm = "master";

        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }
    }

    public static class Realm {

        private boolean autoCreate;
        private boolean autoCreateFailFast = true;

        public boolean isAutoCreate() {
            return autoCreate;
        }

        public void setAutoCreate(boolean autoCreate) {
            this.autoCreate = autoCreate;
        }

        public boolean isAutoCreateFailFast() {
            return autoCreateFailFast;
        }

        public void setAutoCreateFailFast(boolean autoCreateFailFast) {
            this.autoCreateFailFast = autoCreateFailFast;
        }
    }
}

