const { loadConfig } = require('../src/config');
const { createClient, request } = require('../src/httpClient');

function randomSuffix() {
  return `${Date.now()}${Math.floor(Math.random() * 1000)}`;
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
  console.log('[auth] POST /auth/register');
  await request(authClient, {
    method: 'post',
    url: '/auth/register',
    data: {
      username: user.username,
      email: user.email,
      password: cfg.testUserPassword,
      firstName: 'Integration',
      lastName: 'User'
    }
  }, [201]);

  console.log('[auth] POST /auth/login');
  const login = await request(authClient, {
    method: 'post',
    url: '/auth/login',
    data: {
      email: user.email,
      password: cfg.testUserPassword
    }
  }, [200]);

  if (!login.data.accessToken) {
    throw new Error('Login succeeded but accessToken is missing');
  }

  return login.data.accessToken;
}

async function resolveTenant(projectClient, username) {
  console.log('[project] POST /tenants');
  const create = await request(projectClient, {
    method: 'post',
    url: '/tenants',
    data: {
      name: `${username}-workspace`,
      description: 'Integration test tenant',
      maxUsers: 25,
      authenticationEnabled: false
    }
  }, [201, 409]);

  if (create.status === 201) {
    return create.data;
  }

  console.log('[project] tenant exists -> GET /tenants/owner/{username}');
  const list = await request(projectClient, {
    method: 'get',
    url: `/tenants/owner/${encodeURIComponent(username)}?page=0&size=10`
  }, [200]);

  const first = list.data && list.data.content && list.data.content[0];
  if (!first || !first.id) {
    throw new Error('Expected existing tenant but none was returned');
  }
  return first;
}

async function createApplications(projectClient, tenantId) {
  const appKinds = ['PERSONAL_WEB_APP', 'TOOLBOX_WEBAPP', 'ECOMMERCE_APP'];

  for (const kind of appKinds) {
    const appName = `${kind.toLowerCase().replace(/_/g, '-')}-${randomSuffix()}`;
    console.log(`[project] POST /tenants/${tenantId}/applications (${kind})`);

    const created = await request(projectClient, {
      method: 'post',
      url: `/tenants/${tenantId}/applications`,
      data: {
        name: appName,
        kind,
        description: `Integration app of kind ${kind}`,
        domain: ''
      }
    }, [201]);

    if (!created.data || !created.data.id) {
      throw new Error(`Application create returned no id for kind=${kind}`);
    }
  }

  console.log(`[project] GET /tenants/${tenantId}/applications`);
  const apps = await request(projectClient, {
    method: 'get',
    url: `/tenants/${tenantId}/applications?page=0&size=20`
  }, [200]);

  const count = apps.data && apps.data.content ? apps.data.content.length : 0;
  if (count < 3) {
    throw new Error(`Expected at least 3 applications after creates, got ${count}`);
  }
}

async function adminChecks(cfg, authClient) {
  if (!cfg.adminEmail || !cfg.adminPassword) {
    console.log('[admin] ADMIN_EMAIL/ADMIN_PASSWORD not set -> skipping admin checks');
    return;
  }

  console.log('[admin] login');
  const adminLogin = await request(authClient, {
    method: 'post',
    url: '/auth/login',
    data: {
      email: cfg.adminEmail,
      password: cfg.adminPassword
    }
  }, [200]);

  const adminToken = adminLogin.data.accessToken;
  const projectAdminClient = createClient(cfg.projectBaseUrl, adminToken);

  console.log('[admin] GET /applications');
  await request(projectAdminClient, {
    method: 'get',
    url: '/applications?page=0&size=20'
  }, [200]);

  if (cfg.userBaseUrl) {
    const userAdminClient = createClient(cfg.userBaseUrl, adminToken);
    console.log('[admin] GET /users');
    await request(userAdminClient, {
      method: 'get',
      url: '/users?page=0&size=20'
    }, [200]);
  }
}

async function main() {
  const cfg = loadConfig();
  const authClient = createClient(cfg.authBaseUrl);
  const initClient = createClient(cfg.initBaseUrl);

  const suffix = randomSuffix();
  const user = {
    username: `it-user-${suffix}`,
    email: `it-user-${suffix}@example.com`
  };

  await maybeInitialize(initClient, cfg);
  const token = await registerAndLogin(authClient, cfg, user);

  const projectClient = createClient(cfg.projectBaseUrl, token);
  const tenant = await resolveTenant(projectClient, user.username);
  await createApplications(projectClient, tenant.id);

  await adminChecks(cfg, authClient);

  console.log('Integration flow passed.');
}

main().catch((err) => {
  console.error('Integration flow failed:', err.message);
  process.exit(1);
});

