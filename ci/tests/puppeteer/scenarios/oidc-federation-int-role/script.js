const cas = require("../../cas.js");
const assert = require("assert");

(async () => {

    const endpointUrl = "https://localhost:8443/cas/oidc/.well-known/openid-federation";
    const jwt = await cas.doGet(endpointUrl,
        (res) => res.data,
        (error) => {
            throw `Federation endpoint not available: ${error}`;
        });

    const decoded = await cas.decodeJwt(jwt);
    await cas.log(decoded);
    assert(decoded.iss !== undefined);
    assert(decoded.sub !== undefined);
    assert(decoded.exp !== undefined);
    assert(decoded.jwks !== undefined);

    const metadata = decoded.metadata;
    const federationEntity = metadata["federation_entity"];
    assert(federationEntity !== undefined);
    assert(metadata["openid_provider"] === undefined);
    assert(decoded["authority_hints"] !== undefined);

    assert(federationEntity["organization_name"] === "ApereoINT");
    assert(federationEntity["federation_fetch_endpoint"] === "https://localhost:8443/cas/oidc/fetch");
    assert(federationEntity["contacts"] !== undefined);

    const fetchUrl = "https://localhost:8443/cas/oidc/fetch?sub=http%3A%2F%2Frp";
    const jwt2 = await cas.doGet(fetchUrl,
        (res) => res.data,
        (error) => {
            throw `Fetch endpoint not available: ${error}`;
        });

    const decoded2 = await cas.decodeJwt(jwt2);
    await cas.log(decoded2);
    assert(decoded2.iss === "https://localhost:8443/cas/oidc");
    assert(decoded2.sub === "http://rp");
    assert(decoded2.exp !== undefined);
    assert(decoded2.jwks !== undefined);

    const metadata2 = decoded2.metadata;
    assert(metadata2["federation_entity"] === undefined);
    assert(metadata2["openid_provider"] === undefined);
    assert(metadata2["openid_relying_party"] !== undefined);
    assert(decoded2["authority_hints"] === undefined);

    const listUrl = "https://localhost:8443/cas/oidc/list";
    await cas.doGet(listUrl,
        (res) => res.data,
        (error) => {
            throw `List endpoint not available: ${error}`;
        });

})();
