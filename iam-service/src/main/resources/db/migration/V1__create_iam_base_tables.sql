CREATE TABLE IF NOT EXISTS tenant_realm_config (
    tenant_id UUID PRIMARY KEY,
    identity_mode VARCHAR(32) NOT NULL,
    realm_name VARCHAR(128) NOT NULL,
    dedicated_realm_approved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS platform_user (
    id UUID PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tenant_realm_config_identity_mode
    ON tenant_realm_config (identity_mode);
