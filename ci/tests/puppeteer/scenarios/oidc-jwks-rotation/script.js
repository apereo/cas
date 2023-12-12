const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const body = await cas.doRequest("https://localhost:8443/cas/oidc/jwks", "GET", {}, 200);
    assert(JSON.parse(body).keys.length === 4);
    await cas.logg("Rotating keys...");
    await cas.doRequest("https://localhost:8443/cas/actuator/oidcJwks/rotate", "GET", {}, 200);
    await cas.logg("Revoking keys...");
    await cas.doRequest("https://localhost:8443/cas/actuator/oidcJwks/revoke", "GET", {}, 200);
    await cas.logg("Fetching all current keys...");
    await cas.doRequest("https://localhost:8443/cas/oidc/jwks?state=current", "GET", {}, 200);

})();
