const { loadConfig } = require('../src/config');
const { createClient, request } = require('../src/httpClient');

function assert(condition, message) {
  if (!condition) {
    throw new Error(`Assertion failed: ${message}`);
  }
}

function normalizeKeycloakBaseUrl(url) {
  if (!url) {
    return '';
  }

  let normalized = url.trim();
  if (normalized.endsWith('/')) {
    normalized = normalized.slice(0, -1);
  }

  if (normalized.endsWith('/api')) {
    normalized = normalized.slice(0, -4);
  }

  return normalized;
}

function resolveKeycloakBaseUrl(cfg) {
  const preferred = process.env.KEYCLOAK_BASE_URL;
  if (preferred?.trim()) {
    return normalizeKeycloakBaseUrl(preferred);
  }

  const fromAuth = normalizeKeycloakBaseUrl(cfg.authBaseUrl || '');
  if (fromAuth && !fromAuth.includes(':8000') && !fromAuth.includes(':10001')) {
    return fromAuth;
  }

  return 'http://localhost:8080';
}

function loadKeycloakAdminConfig() {
  const cfg = loadConfig();

  return {
    cfg,
    keycloakBaseUrl: resolveKeycloakBaseUrl(cfg),
    adminUsername: process.env.KEYCLOAK_ADMIN || 'admin',
    adminPassword: process.env.KEYCLOAK_ADMIN_PASSWORD || 'admin',
    adminClientId: process.env.KEYCLOAK_ADMIN_CLIENT_ID || 'admin-cli'
  };
}

async function getAdminAccessToken(adminCfg) {
  const client = createClient(adminCfg.keycloakBaseUrl);
  const body = new URLSearchParams({
    grant_type: 'password',
    client_id: adminCfg.adminClientId,
    username: adminCfg.adminUsername,
    password: adminCfg.adminPassword
  });

  const response = await request(client, {
    method: 'post',
    url: '/realms/master/protocol/openid-connect/token',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    data: body.toString()
  }, [200]);

  assert(response.data?.access_token, 'missing Keycloak admin access token');
  return response.data.access_token;
}

function createAdminApiClient(adminCfg, accessToken) {
  return createClient(adminCfg.keycloakBaseUrl, accessToken);
}

async function getRealm(adminClient, realmName) {
  const response = await request(adminClient, {
    method: 'get',
    url: `/admin/realms/${realmName}`
  }, [200, 404]);

  if (response.status === 404) {
    return null;
  }

  return response.data;
}

async function getClientByClientId(adminClient, realmName, clientId) {
  const response = await request(adminClient, {
    method: 'get',
    url: `/admin/realms/${realmName}/clients`,
    params: { clientId }
  }, [200]);

  const matches = Array.isArray(response.data)
    ? response.data.filter((it) => it.clientId === clientId)
    : [];

  return matches[0] || null;
}

async function getUserByUsername(adminClient, realmName, username) {
  const response = await request(adminClient, {
    method: 'get',
    url: `/admin/realms/${realmName}/users`,
    params: { username }
  }, [200]);

  const matches = Array.isArray(response.data)
    ? response.data.filter((it) => it.username === username)
    : [];

  return matches[0] || null;
}

async function createAdminContext() {
  const adminCfg = loadKeycloakAdminConfig();
  const accessToken = await getAdminAccessToken(adminCfg);
  const adminClient = createAdminApiClient(adminCfg, accessToken);

  return { adminCfg, adminClient };
}

module.exports = {
  assert,
  createAdminContext,
  getRealm,
  getClientByClientId,
  getUserByUsername
};
