package com.avira.userservice.config.properties;

import com.avira.commonlib.config.properties.KeycloakProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakPropertiesTest {

    @Test
    void shouldFallbackAuthSettingsToSyncRealmDefaults() {
        KeycloakProperties properties = new KeycloakProperties();
        properties.getSync().setRealm("avira");

        assertThat(properties.getResolvedAuthRealm()).isEqualTo("avira");
        assertThat(properties.getResolvedAuthClientId()).isEqualTo("avira-user-client");
        assertThat(properties.getResolvedAuthClientSecret()).isEmpty();
    }

    @Test
    void shouldPreferExplicitAuthSettingsWhenProvided() {
        KeycloakProperties properties = new KeycloakProperties();
        properties.getSync().setRealm("avira");
        properties.getAuth().setRealm("custom-realm");
        properties.getAuth().setClientId("custom-client");
        properties.getAuth().setClientSecret("secret");

        assertThat(properties.getResolvedAuthRealm()).isEqualTo("custom-realm");
        assertThat(properties.getResolvedAuthClientId()).isEqualTo("custom-client");
        assertThat(properties.getResolvedAuthClientSecret()).isEqualTo("secret");
    }
}

