const axios = require('axios');

function createClient(baseURL, token) {
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return axios.create({
    baseURL,
    timeout: 20000,
    headers
  });
}

async function request(client, config, okStatusCodes) {
  try {
    const response = await client.request(config);
    if (okStatusCodes && !okStatusCodes.includes(response.status)) {
      throw new Error(`Unexpected status ${response.status}`);
    }
    return response;
  } catch (error) {
    const details = error.response
      ? `status=${error.response.status} body=${JSON.stringify(error.response.data)}`
      : error.message;
    throw new Error(`HTTP ${config.method || 'GET'} ${config.url} failed: ${details}`);
  }
}

module.exports = { createClient, request };

