const cas = require("../../cas.js");
const assert = require("assert");

(async () => {

    const url = "https://localhost:8443/cas/rp/oidcprovider/.well-known/openid-federation";
    const jwt = await cas.doGet(url,
        (res) => res.data,
        (error) => {
            throw `Federation endpoint not available: ${error}`;
        });

    const decoded = await cas.decodeJwt(jwt);
    cas.log(decoded);
    assert(decoded.iss !== undefined);
    assert(decoded.sub !== undefined);
    assert(decoded.exp !== undefined);
    assert(decoded.jwks !== undefined);
    const rp = decoded.metadata["openid_relying_party"];
    assert(rp.jwks !== undefined);
    assert(rp.token_endpoint_auth_method === "private_key_jwt");

})();
