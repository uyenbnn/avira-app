const {
  assert,
  createAdminContext,
  getClientByClientId
} = require('./_keycloak-admin-common');

async function assertClient(adminClient, realmName, clientId, expectation) {
  const client = await getClientByClientId(adminClient, realmName, clientId);
  assert(client, `client must exist: ${realmName}/${clientId}`);

  if (expectation === 'confidential') {
    assert(client.publicClient === false, `client must be confidential: ${realmName}/${clientId}`);
    assert(client.serviceAccountsEnabled === true, `confidential client must enable service accounts: ${realmName}/${clientId}`);
  } else {
    assert(client.publicClient === true, `client must be public: ${realmName}/${clientId}`);
  }

  console.log(`PASS client=${realmName}/${clientId} type=${expectation}`);
}

async function main() {
  console.log('[keycloak] validate required clients');
  const { adminCfg, adminClient } = await createAdminContext();
  console.log(`[keycloak] admin base url: ${adminCfg.keycloakBaseUrl}`);

  const checks = [
    { realm: 'saas', clientId: 'saas-backend', type: 'confidential' },
    { realm: 'saas', clientId: 'saas-console', type: 'public' },
    { realm: 'avira-platform', clientId: 'avira-platform-backend', type: 'confidential' },
    { realm: 'avira-platform', clientId: 'avira-platform-public', type: 'public' }
  ];

  for (const check of checks) {
    await assertClient(adminClient, check.realm, check.clientId, check.type);
  }
}

main().catch((err) => {
  console.error('FAIL keycloak-clients-provisioned:', err.message);
  process.exit(1);
});
