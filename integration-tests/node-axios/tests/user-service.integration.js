const { loadConfig } = require('../src/config');
const { createClient, request } = require('../src/httpClient');

function randomSuffix() {
  return `${Date.now()}${Math.floor(Math.random() * 1000)}`;
}

function decodeJwtPayload(token) {
  const parts = token.split('.');
  if (parts.length < 2) {
    throw new Error('Invalid JWT token format');
  }
  const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/');
  const normalized = payload + '='.repeat((4 - (payload.length % 4 || 4)) % 4);
  return JSON.parse(Buffer.from(normalized, 'base64').toString('utf8'));
}

async function maybeInitialize(initClient, cfg) {
  if (!cfg.runInit) {
    console.log('[init] RUN_INIT=false -> skipping init calls');
    return;
  }

  console.log('[init] POST /init/keycloak');
  await request(initClient, { method: 'post', url: '/init/keycloak' }, [200]);

  console.log('[init] POST /init/messaging');
  await request(initClient, { method: 'post', url: '/init/messaging' }, [200]);
}

async function registerAndLogin(authClient, cfg, user) {
  await request(authClient, {
    method: 'post',
    url: '/auth/register',
    data: {
      username: user.username,
      email: user.email,
      password: cfg.testUserPassword,
      firstName: user.firstName,
      lastName: user.lastName
    }
  }, [201]);

  const login = await request(authClient, {
    method: 'post',
    url: '/auth/login',
    data: {
      email: user.email,
      password: cfg.testUserPassword
    }
  }, [200]);

  return login.data.accessToken;
}

async function loginAdmin(authClient, cfg) {
  if (!cfg.adminEmail || !cfg.adminPassword) {
    throw new Error('ADMIN_EMAIL and ADMIN_PASSWORD are required for user-service integration test');
  }
  const adminLogin = await request(authClient, {
    method: 'post',
    url: '/auth/login',
    data: {
      email: cfg.adminEmail,
      password: cfg.adminPassword
    }
  }, [200]);
  return adminLogin.data.accessToken;
}

async function assertForbidden(client, path) {
  try {
    await client.get(path);
    throw new Error(`Expected 403 for ${path} but request succeeded`);
  } catch (err) {
    const status = err.response && err.response.status;
    if (status !== 403) {
      throw new Error(`Expected 403 for ${path}, got ${status || err.message}`);
    }
  }
}

async function main() {
  const cfg = loadConfig();
  if (!cfg.userBaseUrl) {
    throw new Error('USER_BASE_URL is required for user-service integration test');
  }

  const authClient = createClient(cfg.authBaseUrl);
  const initClient = createClient(cfg.initBaseUrl);

  await maybeInitialize(initClient, cfg);

  const sfx1 = randomSuffix();
  const sfx2 = randomSuffix();

  const user1 = {
    username: `it-u1-${sfx1}`,
    email: `it-u1-${sfx1}@example.com`,
    firstName: 'User',
    lastName: 'One'
  };

  const user2 = {
    username: `it-u2-${sfx2}`,
    email: `it-u2-${sfx2}@example.com`,
    firstName: 'User',
    lastName: 'Two'
  };

  console.log('[auth] register/login user1 + user2');
  const token1 = await registerAndLogin(authClient, cfg, user1);
  const token2 = await registerAndLogin(authClient, cfg, user2);
  const adminToken = await loginAdmin(authClient, cfg);

  const user1Id = decodeJwtPayload(token1).sub;
  const user2Id = decodeJwtPayload(token2).sub;

  const userClient1 = createClient(cfg.userBaseUrl, token1);
  const userClient2 = createClient(cfg.userBaseUrl, token2);
  const adminUserClient = createClient(cfg.userBaseUrl, adminToken);

  console.log('[user-service] verify admin-only GET /users');
  await assertForbidden(userClient1, '/users?page=0&size=10');
  await request(adminUserClient, { method: 'get', url: '/users?page=0&size=10' }, [200]);

  console.log('[user-service] verify self-only GET /users/{id}');
  await request(userClient1, { method: 'get', url: `/users/${user1Id}` }, [200]);
  await assertForbidden(userClient1, `/users/${user2Id}`);
  await request(adminUserClient, { method: 'get', url: `/users/${user2Id}` }, [200]);

  console.log('[user-service] CRUD by admin on business user');
  const created = await request(adminUserClient, {
    method: 'post',
    url: '/users',
    data: {
      email: `it-business-${randomSuffix()}@example.com`,
      phone: '123456789',
      firstName: 'Biz',
      lastName: 'User'
    }
  }, [201]);

  const createdId = created.data.id;
  await request(adminUserClient, {
    method: 'put',
    url: `/users/${createdId}`,
    data: { phone: '987654321' }
  }, [200]);

  await request(adminUserClient, { method: 'get', url: `/users/${createdId}` }, [200]);

  await request(adminUserClient, {
    method: 'patch',
    url: `/users/${createdId}/status?status=DISABLED`
  }, [204]);

  await request(adminUserClient, {
    method: 'delete',
    url: `/users/${createdId}`
  }, [204]);

  console.log('User-service integration flow passed.');
}

main().catch((err) => {
  console.error('User-service integration flow failed:', err.message);
  process.exit(1);
});


