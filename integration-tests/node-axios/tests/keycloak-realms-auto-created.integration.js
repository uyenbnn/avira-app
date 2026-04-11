const {
  assert,
  createAdminContext,
  getRealm
} = require('./_keycloak-admin-common');

async function main() {
  console.log('[keycloak] validate auto-created realms');
  const { adminCfg, adminClient } = await createAdminContext();
  console.log(`[keycloak] admin base url: ${adminCfg.keycloakBaseUrl}`);

  const requiredRealms = ['saas', 'avira-platform'];
  for (const realmName of requiredRealms) {
    const realm = await getRealm(adminClient, realmName);
    assert(realm, `realm must exist: ${realmName}`);
    assert(realm.realm === realmName, `realm mismatch for ${realmName}`);
    assert(realm.enabled === true, `realm must be enabled: ${realmName}`);
    console.log(`PASS realm=${realmName}`);
  }
}

main().catch((err) => {
  console.error('FAIL keycloak-realms-auto-created:', err.message);
  process.exit(1);
});
