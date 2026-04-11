const { assert, createMvpClients, request } = require('./_mvp-common');

async function main() {
  const { appClient } = createMvpClients();

  console.log('[mvp] app token exchange endpoint shape');

  const appId = 'mvp-app-shape';
  const tenantId = 'tenant-shape-check';

  const response = await request(appClient, {
    method: 'post',
    url: `/api/apps/${appId}/auth/token-exchange`,
    data: {
      subjectToken: 'subject-token-sample',
      authMode: 'KEYCLOAK'
    },
    headers: {
      'X-Tenant-Id': tenantId
    }
  }, [200]);

  assert(response.status === 200, `expected status 200, got ${response.status}`);
  assert(response.data.accessToken, 'accessToken must be present');
  assert(response.data.refreshToken, 'refreshToken must be present');
  assert(typeof response.data.expiresIn === 'number', 'expiresIn must be a number');
  assert(response.data.tokenType === 'Bearer', 'tokenType must be Bearer');
  assert(response.data.appId === appId, 'appId must match request path');
  assert(response.data.tenantId === tenantId, 'tenantId must match token context header');

  console.log(`PASS appId=${appId} tenantId=${tenantId}`);
}

main().catch((err) => {
  console.error('FAIL mvp-app-token-exchange-shape:', err.message);
  process.exit(1);
});
