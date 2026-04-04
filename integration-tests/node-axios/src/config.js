const fs = require('fs');
const path = require('path');
const dotenv = require('dotenv');

function loadConfig() {
  const args = process.argv.slice(2);
  const envArgIndex = args.indexOf('--env');
  const envFile = envArgIndex >= 0 ? args[envArgIndex + 1] : '.env.docker';
  const envPath = path.resolve(__dirname, '..', envFile);

  if (fs.existsSync(envPath)) {
    dotenv.config({ path: envPath });
  }

  const cfg = {
    authBaseUrl: process.env.AUTH_BASE_URL,
    projectBaseUrl: process.env.PROJECT_BASE_URL,
    initBaseUrl: process.env.INIT_BASE_URL,
    userBaseUrl: process.env.USER_BASE_URL || '',
    testUserPassword: process.env.TEST_USER_PASSWORD || 'Test123!',
    adminEmail: process.env.ADMIN_EMAIL || '',
    adminPassword: process.env.ADMIN_PASSWORD || '',
    runInit: String(process.env.RUN_INIT || 'false').toLowerCase() === 'true'
  };

  const required = ['authBaseUrl', 'projectBaseUrl', 'initBaseUrl'];
  for (const key of required) {
    if (!cfg[key]) {
      throw new Error(`Missing required config: ${key}`);
    }
  }

  return cfg;
}

module.exports = { loadConfig };

