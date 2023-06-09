const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    await cas.doGet("https://localhost:8443/cas/oidc/accessToken?grant_type=client_credentials&client_id=clientES512&client_secret=secretES512",
        res => {
            assert(res.status === 200)
        },
        error => {
            throw error;
        });

    await cas.doGet("https://localhost:8443/cas/oidc/accessToken?grant_type=client_credentials&client_id=clientRS256&client_secret=secretRS256",
        res => {
            assert(res.status === 200)
        },
        error => {
            throw error;
        });

})();
