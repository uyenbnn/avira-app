CREATE TABLE applications (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    domain VARCHAR(255),
    kind VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_applications_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT uq_application_tenant_name UNIQUE (tenant_id, name),
    CONSTRAINT uq_application_domain UNIQUE (domain)
);

CREATE INDEX idx_applications_tenant_id ON applications(tenant_id);
CREATE INDEX idx_applications_kind ON applications(kind);
CREATE INDEX idx_applications_status ON applications(status);

