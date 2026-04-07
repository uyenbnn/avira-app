/**
 * IAM Service Integration Tests
 *
 * Tests all IAM endpoints:
 *   POST /api/iam/auth/login
 *   POST /api/iam/auth/refresh
 *   POST /api/iam/auth/logout
 *   POST /api/iam/users
 *   GET  /api/iam/users
 *   GET  /api/iam/users/:id
 *   POST /api/iam/init/tenants
 *   GET  /api/iam/realms/tenants/:tenantId
 *
 * Run:
 *   node tests/iam-service.integration.js --env .env.docker
 *   node tests/iam-service.integration.js --env .env.kong
 */

const { v4: uuidv4 } = require('uuid');
const { loadConfig } = require('../src/config');
const { createClient, request } = require('../src/httpClient');

// ─── Helpers ──────────────────────────────────────────────────────────────────

function randomUsername() {
  return 'user_' + Date.now();
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(`Assertion failed: ${message}`);
  }
}

function assertStatus(response, expected, label) {
  assert(
    response.status === expected,
    `${label}: expected status ${expected}, got ${response.status}`
  );
}

// ─── Test Suite ───────────────────────────────────────────────────────────────

async function testLogin(client, adminEmail, adminPassword) {
  console.log('  → POST /api/iam/auth/login');
  const res = await request(client, {
    method: 'POST',
    url: '/api/iam/auth/login',
    data: { username: adminEmail, password: adminPassword }
  }, [200]);

  assertStatus(res, 200, 'Login');
  assert(res.data.accessToken, 'Login: accessToken should be present');
  assert(res.data.refreshToken, 'Login: refreshToken should be present');
  console.log('    ✓ Login returned access + refresh tokens');
  return res.data;
}

async function testRefresh(client, refreshToken) {
  console.log('  → POST /api/iam/auth/refresh');
  const res = await request(client, {
    method: 'POST',
    url: '/api/iam/auth/refresh',
    data: { refreshToken }
  }, [200]);

  assertStatus(res, 200, 'Refresh');
  assert(res.data.accessToken, 'Refresh: new accessToken should be present');
  console.log('    ✓ Refresh returned new access token');
  return res.data;
}

async function testLogout(client, refreshToken) {
  console.log('  → POST /api/iam/auth/logout');
  const res = await request(client, {
    method: 'POST',
    url: '/api/iam/auth/logout',
    data: { refreshToken }
  }, [204]);

  assertStatus(res, 204, 'Logout');
  console.log('    ✓ Logout returned 204 No Content');
}

async function testCreateUser(client, username) {
  console.log('  → POST /api/iam/users');
  const res = await request(client, {
    method: 'POST',
    url: '/api/iam/users',
    data: {
      username,
      email: `${username}@avira-test.local`
    }
  }, [201]);

  assertStatus(res, 201, 'Create platform user');
  assert(res.data.id, 'Create user: id should be present');
  assert(res.data.username === username, 'Create user: username should match');
  assert(res.data.email === `${username}@avira-test.local`, 'Create user: email should match');
  assert(res.data.status === 'ACTIVE', 'Create user: status should be ACTIVE');
  console.log(`    ✓ User created: id=${res.data.id}`);
  return res.data;
}

async function testGetUser(client, userId) {
  console.log(`  → GET /api/iam/users/${userId}`);
  const res = await request(client, {
    method: 'GET',
    url: `/api/iam/users/${userId}`
  }, [200]);

  assertStatus(res, 200, 'Get user by id');
  assert(res.data.id === userId, 'Get user: id should match');
  console.log(`    ✓ User retrieved: username=${res.data.username}`);
  return res.data;
}

async function testGetAllUsers(client) {
  console.log('  → GET /api/iam/users');
  const res = await request(client, {
    method: 'GET',
    url: '/api/iam/users'
  }, [200]);

  assertStatus(res, 200, 'Get all users');
  assert(Array.isArray(res.data), 'Get all users: response should be an array');
  console.log(`    ✓ Got ${res.data.length} user(s)`);
  return res.data;
}

async function testInitTenant(client, tenantId) {
  console.log('  → POST /api/iam/init/tenants (SHARED_REALM)');
  const res = await request(client, {
    method: 'POST',
    url: '/api/iam/init/tenants',
    data: {
      tenantId,
      identityMode: 'SHARED_REALM'
    }
  }, [201]);

  assertStatus(res, 201, 'Init tenant realm');
  assert(res.data.tenantId === tenantId, 'Init tenant: tenantId should match');
  assert(res.data.identityMode === 'SHARED_REALM', 'Init tenant: identityMode should be SHARED_REALM');
  assert(res.data.realmName === 'avira-platform', 'Init tenant: realmName should be avira-platform for shared');
  console.log(`    ✓ Tenant realm initialized: realm=${res.data.realmName}`);
  return res.data;
}

async function testInitDedicatedTenant(client, tenantId) {
  console.log('  → POST /api/iam/init/tenants (DEDICATED_REALM)');
  const res = await request(client, {
    method: 'POST',
    url: '/api/iam/init/tenants',
    data: {
      tenantId,
      identityMode: 'DEDICATED_REALM'
    }
  }, [201]);

  assertStatus(res, 201, 'Init dedicated tenant realm');
  assert(res.data.identityMode === 'DEDICATED_REALM', 'Init dedicated tenant: identityMode should be DEDICATED_REALM');
  assert(
    res.data.realmName === `tenant_${tenantId}`,
    `Init dedicated tenant: realmName should be tenant_${tenantId}`
  );
  console.log(`    ✓ Dedicated tenant realm initialized: realm=${res.data.realmName}`);
  return res.data;
}

async function testResolveRealm(client, tenantId, expectedRealm) {
  console.log(`  → GET /api/iam/realms/tenants/${tenantId}`);
  const res = await request(client, {
    method: 'GET',
    url: `/api/iam/realms/tenants/${tenantId}`
  }, [200]);

  assertStatus(res, 200, 'Resolve realm');
  assert(res.data.realm === expectedRealm, `Resolve realm: expected realm=${expectedRealm}, got=${res.data.realm}`);
  console.log(`    ✓ Realm resolved: ${res.data.realm}`);
  return res.data;
}

// ─── Main Runner ──────────────────────────────────────────────────────────────

async function run() {
  const config = loadConfig();

  if (!config.iamBaseUrl) {
    console.log('⚠  IAM_BASE_URL not set – skipping IAM tests');
    process.exit(0);
  }

  console.log(`\n=== IAM Service Integration Tests ===`);
  console.log(`Base URL: ${config.iamBaseUrl}\n`);

  const client = createClient(config.iamBaseUrl);

  let passed = 0;
  let failed = 0;

  async function run(name, fn) {
    try {
      await fn();
      passed++;
    } catch (err) {
      console.error(`  ✗ ${name}: ${err.message}`);
      failed++;
    }
  }

  // ─── Auth Flow ──────────────────────────────────────────────────────────────
  let accessToken = null;
  let refreshToken = null;

  await run('Login', async () => {
    const tokens = await testLogin(client, config.adminEmail, config.adminPassword);
    accessToken = tokens.accessToken;
    refreshToken = tokens.refreshToken;
  });

  await run('Refresh token', async () => {
    if (!refreshToken) throw new Error('No refresh token – login may have failed');
    const tokens = await testRefresh(client, refreshToken);
    refreshToken = tokens.refreshToken || refreshToken;
  });

  // ─── Platform User CRUD ─────────────────────────────────────────────────────
  let createdUser = null;
  const username = randomUsername();

  await run('Create platform user', async () => {
    createdUser = await testCreateUser(client, username);
  });

  await run('Get all platform users', async () => {
    await testGetAllUsers(client);
  });

  await run('Get platform user by id', async () => {
    if (!createdUser) throw new Error('No user created – create may have failed');
    await testGetUser(client, createdUser.id);
  });

  // ─── Tenant Init & Realm Resolution ─────────────────────────────────────────
  const sharedTenantId = uuidv4();
  const dedicatedTenantId = uuidv4();

  await run('Init shared realm tenant', async () => {
    await testInitTenant(client, sharedTenantId);
  });

  await run('Resolve shared realm', async () => {
    await testResolveRealm(client, sharedTenantId, 'avira-platform');
  });

  await run('Init dedicated realm tenant', async () => {
    await testInitDedicatedTenant(client, dedicatedTenantId);
  });

  await run('Resolve dedicated realm', async () => {
    await testResolveRealm(client, dedicatedTenantId, `tenant_${dedicatedTenantId}`);
  });

  // ─── Logout ─────────────────────────────────────────────────────────────────
  await run('Logout', async () => {
    if (!refreshToken) throw new Error('No refresh token – login may have failed');
    await testLogout(client, refreshToken);
  });

  // ─── Result Summary ─────────────────────────────────────────────────────────
  console.log(`\n─── Results: ${passed} passed, ${failed} failed ───`);
  if (failed > 0) {
    process.exit(1);
  }
}

run().catch(err => {
  console.error('Unexpected error:', err.message);
  process.exit(1);
});

