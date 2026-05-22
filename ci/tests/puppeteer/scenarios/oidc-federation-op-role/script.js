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
    const federationEntity = metadata["federation_entity"];
    assert(federationEntity !== undefined);
    const openidProvider = metadata["openid_provider"];
    assert(openidProvider !== undefined);
    assert(decoded["authority_hints"] !== undefined);

    assert(federationEntity["organization_name"] === "ApereoOP");
    assert(federationEntity["federation_fetch_endpoint"] === undefined);
    assert(federationEntity["contacts"] !== undefined);

    const algValues = openidProvider["userinfo_signing_alg_values_supported"];
    assert(algValues.includes("RS256"));
    assert(algValues.includes("HS256"));

    const listUrl = "https://localhost:8443/cas/oidc/list";
    await cas.doGet(listUrl,
        () => {
            throw "Federation endpoint should not be available";
        },
        () => {
        });

})();
