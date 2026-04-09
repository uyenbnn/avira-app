const { loadConfig } = require('../src/config');

try {
  const cfg = loadConfig();
  const mustBeUrls = [cfg.authBaseUrl, cfg.projectBaseUrl, cfg.initBaseUrl];
  mustBeUrls.forEach((value) => new URL(value));
  console.log('Config validation passed.');
  process.exit(0);
} catch (err) {
  console.error('Config validation failed:', err.message);
  process.exit(1);
}

