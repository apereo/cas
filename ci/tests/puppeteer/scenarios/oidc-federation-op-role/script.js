const cas = require("../../cas.js");
const assert = require("assert");

(async () => {

    const url = "https://localhost:8443/cas/oidc/.well-known/openid-federation";
    const jwt = await cas.doGet(url,
        (res) => res.data,
        (error) => {
            throw `Federation endpoint not available: ${error}`;
        });

    const decoded = await cas.decodeJwt(jwt);
    await cas.log(decoded);
    const iss = decoded.iss;
    const sub = decoded.sub;
    assert(iss !== undefined);
    assert(sub === iss);
    assert(decoded.exp !== undefined);
    assert(decoded.jwks !== undefined);
    const metadata = decoded.metadata;
    assert(metadata["federation_entity"] !== undefined);
    assert(metadata["openid_provider"] !== undefined);
    assert(decoded["authority_hints"] !== undefined);

})();
