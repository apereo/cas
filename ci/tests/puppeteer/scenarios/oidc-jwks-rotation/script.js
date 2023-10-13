const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    await cas.doGet("https://localhost:8443/cas/oidc/jwks",
        async res => {
            assert(res.status === 200);
            assert(res.data.keys.length === 4);
        },
        async error => {
            throw error;
        });

    await cas.logg("Rotating keys...");
    await cas.doGet("https://localhost:8443/cas/actuator/oidcJwks/rotate",
        async res => assert(res.status === 200),
        async error => {
            throw error;
        });

    await cas.logg("Revoking keys...");
    await cas.doGet("https://localhost:8443/cas/actuator/oidcJwks/revoke",
        async res => assert(res.status === 200),
        async error => {
            throw error;
        });

    await cas.logg("Fetching all current keys...");
    await cas.doGet("https://localhost:8443/cas/oidc/jwks?state=current",
        async res => assert(res.status === 200),
        async error => {
            throw error;
        })
})();
