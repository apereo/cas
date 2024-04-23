const cas = require("../../cas.js");

(async () => {
    await cas.logg("Rotating keys...");
    await cas.doRequest("https://localhost:8443/cas/actuator/oidcJwks/rotate", "GET", {}, 200);

    await cas.logg("Revoking keys...");
    await cas.doRequest("https://localhost:8443/cas/actuator/oidcJwks/revoke", "GET", {}, 200);

    await cas.logg("Fetching all current keys...");
    await cas.doRequest("https://localhost:8443/cas/oidc/jwks?state=current", "GET", {}, 200);
})();
