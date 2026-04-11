const { assert, createMvpClients, createTenant, request } = require('./_mvp-common');

async function main() {
  const { platformClient } = createMvpClients();

  console.log('[mvp] application creation under tenant');
  const tenant = await createTenant(platformClient);

  const appCreate = await request(platformClient, {
    method: 'post',
    url: `/api/platform/tenants/${tenant.tenantId}/applications`,
    data: {
      name: `app-${Date.now()}`,
      domain: `app-${Date.now()}.example.test`,
      authMode: 'KEYCLOAK',
      config: { tier: 'mvp' }
    },
    headers: {
      'X-Tenant-Id': tenant.tenantId
    }
  }, [201]);

  assert(appCreate.status === 201, `expected status 201, got ${appCreate.status}`);
  assert(appCreate.data.appId, 'appId must be present');
  assert(appCreate.data.tenantId === tenant.tenantId, 'tenantId must match created tenant');
  assert(appCreate.data.authMode === 'KEYCLOAK', 'authMode must be KEYCLOAK');

  const appList = await request(platformClient, {
    method: 'get',
    url: `/api/platform/tenants/${tenant.tenantId}/applications`,
    headers: {
      'X-Tenant-Id': tenant.tenantId
    }
  }, [200]);

  assert(appList.status === 200, `expected list status 200, got ${appList.status}`);
  assert(Array.isArray(appList.data), 'applications list must be an array');
  assert(appList.data.some((item) => item.appId === appCreate.data.appId), 'new application must appear in list');

  console.log(`PASS tenantId=${tenant.tenantId} appId=${appCreate.data.appId}`);
}

main().catch((err) => {
  console.error('FAIL mvp-application-creation-under-tenant:', err.message);
  process.exit(1);
});
