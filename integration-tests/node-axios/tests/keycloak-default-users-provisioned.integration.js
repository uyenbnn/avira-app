const {
  assert,
  createAdminContext,
  getUserByUsername
} = require('./_keycloak-admin-common');

async function main() {
  console.log('[keycloak] validate required default users');
  const { adminCfg, adminClient } = await createAdminContext();
  console.log(`[keycloak] admin base url: ${adminCfg.keycloakBaseUrl}`);

  const saasAdmin = await getUserByUsername(adminClient, 'saas', 'saas-admin');
  assert(saasAdmin, 'user must exist: saas/saas-admin');
  assert(saasAdmin.enabled === true, 'saas-admin must be enabled');
  console.log('PASS user=saas/saas-admin');

  const anonymousSaas = await getUserByUsername(adminClient, 'saas', 'anonymous-saas');
  assert(anonymousSaas, 'user must exist: saas/anonymous-saas');
  console.log('PASS user=saas/anonymous-saas');

  const anonymousPlatform = await getUserByUsername(adminClient, 'avira-platform', 'anonymous-platform');
  assert(anonymousPlatform, 'user must exist: avira-platform/anonymous-platform');
  console.log('PASS user=avira-platform/anonymous-platform');
}

main().catch((err) => {
  console.error('FAIL keycloak-default-users-provisioned:', err.message);
  process.exit(1);
});
