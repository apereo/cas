const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    await cas.log("Accessing discover via the configured issuer");
    await cas.doGet("https://localhost:8443/cas/oidc/.well-known/openid-configuration",
        (res) => {
            assert(res.status === 200);
            assert(res.data.issuer !== undefined);
        }, (error) => {
            throw `Operation failed ${error}`;
        });

    await cas.log("Accessing discover via an issuer alias");
    await cas.doGet("http://localhost:8282/cas/oidc/.well-known/openid-configuration",
        (res) => {
            assert(res.status === 200);
            assert(res.data.issuer !== undefined);
        }, (error) => {
            throw `Operation failed ${error}`;
        });

})();
