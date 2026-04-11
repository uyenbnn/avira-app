const { assert, createMvpClients, request } = require('./_mvp-common');

async function main() {
  const { platformClient } = createMvpClients();

  console.log('[mvp] tenant creation (SHARED_REALM)');
  const response = await request(platformClient, {
    method: 'post',
    url: '/api/platform/tenants',
    data: {
      name: `tenant-${Date.now()}`,
      contactEmail: `owner-${Date.now()}@example.test`
    }
  }, [201]);

  assert(response.status === 201, `expected status 201, got ${response.status}`);
  assert(response.data.tenantId, 'tenantId must be present');
  assert(response.data.identityMode === 'SHARED_REALM', 'identityMode must be SHARED_REALM');
  assert(response.data.name, 'name must be present');
  assert(response.data.contactEmail, 'contactEmail must be present');

  console.log(`PASS tenantId=${response.data.tenantId} identityMode=${response.data.identityMode}`);
}

main().catch((err) => {
  console.error('FAIL mvp-tenant-creation-shared-realm:', err.message);
  process.exit(1);
});
