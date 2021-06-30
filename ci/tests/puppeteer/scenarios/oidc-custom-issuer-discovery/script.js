const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');
const axios = require('axios');
const https = require('https');

(async () => {
    const instance = axios.create({
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });

    const issuer = "https://localhost:8443/cas/oidc/custom/issuer/fawnoos";
    instance
        .get(issuer + '/.well-known/openid-configuration')
        .then(res => {
            console.log(res.data);
            let result = res.data;
            assert(result.jwks_uri.startsWith(issuer));
            assert(result.authorization_endpoint.startsWith(issuer));
            assert(result.token_endpoint.startsWith(issuer));
            assert(result.userinfo_endpoint.startsWith(issuer));
            assert(result.userinfo_endpoint.startsWith(issuer));
            assert(result.registration_endpoint.startsWith(issuer));
            assert(result.end_session_endpoint.startsWith(issuer));
            assert(result.introspection_endpoint.startsWith(issuer));
            assert(result.revocation_endpoint.startsWith(issuer));
        })
        .catch(error => {
            throw error;
        })
})();
