const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    await cas.logg("Rotating keys...")
    await cas.doGet("https://localhost:8443/cas/actuator/oidcJwks/rotate",
        res => {
            assert(res.status === 200)
        },
        error => {
            throw error;
        })

    await cas.logg("Revoking keys...")
    await cas.doGet("https://localhost:8443/cas/actuator/oidcJwks/revoke",
        res => {
            assert(res.status === 200)
        },
        error => {
            throw error;
        })

    await cas.logg("Fetching all current keys...")
    await cas.doGet("https://localhost:8443/cas/oidc/jwks?state=current",
        res => {
            assert(res.status === 200)
        },
        error => {
            throw error;
        })
})();
