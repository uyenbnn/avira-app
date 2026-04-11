const { assert, createMvpClients, randomTenantId, request } = require('./_mvp-common');

async function main() {
  const { iamClient } = createMvpClients();

  console.log('[mvp] iam shared realm init/provision');

  const initRes = await request(iamClient, {
    method: 'post',
    url: '/api/iam/internal/init/realms'
  }, [200]);
  assert(initRes.status === 200, `expected init status 200, got ${initRes.status}`);
  assert(initRes.data.realm === 'avira-platform', 'shared realm must be avira-platform');
  assert(
    initRes.data.status === 'INITIALIZED' || initRes.data.status === 'ALREADY_EXISTS',
    'init status must be INITIALIZED or ALREADY_EXISTS'
  );

  const tenantId = randomTenantId();
  const provisionRes = await request(iamClient, {
    method: 'post',
    url: `/api/iam/internal/init/tenants/${tenantId}`,
    data: {
      tenantId,
      tenantName: `tenant-${Date.now()}`,
      contactEmail: `owner-${Date.now()}@example.test`,
      identityMode: 'SHARED_REALM'
    }
  }, [200]);

  assert(provisionRes.status === 200, `expected provision status 200, got ${provisionRes.status}`);
  assert(provisionRes.data.tenantId === tenantId, 'tenantId must match provisioned tenant');
  assert(provisionRes.data.realm === 'avira-platform', 'provisioned realm must be avira-platform');
  assert(provisionRes.data.keycloakClientId, 'keycloakClientId must be present');
  assert(
    provisionRes.data.status === 'PROVISIONED' || provisionRes.data.status === 'ALREADY_EXISTS',
    'provision status must be PROVISIONED or ALREADY_EXISTS'
  );

  console.log(`PASS tenantId=${tenantId} realm=${provisionRes.data.realm}`);
}

main().catch((err) => {
  console.error('FAIL mvp-iam-shared-realm-init-provision:', err.message);
  process.exit(1);
});
