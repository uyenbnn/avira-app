const { v4: uuidv4 } = require('uuid');
const { loadConfig } = require('../src/config');
const { createClient, request } = require('../src/httpClient');

function assert(condition, message) {
  if (!condition) {
    throw new Error(`Assertion failed: ${message}`);
  }
}

function normalizeApiRoot(baseUrl) {
  if (!baseUrl) {
    return baseUrl;
  }
  return baseUrl.endsWith('/api') ? baseUrl.slice(0, -4) : baseUrl;
}

function createMvpClients() {
  const cfg = loadConfig();

  const gatewayBaseUrl = normalizeApiRoot(cfg.mvpGatewayBaseUrl || cfg.authBaseUrl);
  const platformBaseUrl = normalizeApiRoot(cfg.mvpPlatformBaseUrl || cfg.projectBaseUrl || gatewayBaseUrl);
  const iamBaseUrl = normalizeApiRoot(cfg.mvpIamBaseUrl || cfg.iamBaseUrl || gatewayBaseUrl);
  const appBaseUrl = normalizeApiRoot(cfg.mvpAppBaseUrl || gatewayBaseUrl);

  return {
    cfg,
    gatewayClient: createClient(gatewayBaseUrl),
    platformClient: createClient(platformBaseUrl),
    iamClient: createClient(iamBaseUrl),
    appClient: createClient(appBaseUrl)
  };
}

async function createTenant(platformClient) {
  const name = `mvp-tenant-${Date.now()}`;
  const contactEmail = `owner-${Date.now()}@example.test`;

  const response = await request(platformClient, {
    method: 'post',
    url: '/api/platform/tenants',
    data: {
      name,
      contactEmail
    }
  }, [201]);

  return response.data;
}

function randomTenantId() {
  return uuidv4();
}

module.exports = {
  assert,
  request,
  createMvpClients,
  createTenant,
  randomTenantId
};
