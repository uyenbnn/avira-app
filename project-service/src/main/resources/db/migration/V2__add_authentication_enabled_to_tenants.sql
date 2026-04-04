ALTER TABLE tenants
    ADD COLUMN authentication_enabled BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_tenants_authentication_enabled ON tenants(authentication_enabled);

