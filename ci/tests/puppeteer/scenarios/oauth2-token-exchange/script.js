const assert = require("assert");
const cas = require("../../cas.js");

async function exchangeToken(token, fromType, toType) {
    const grantType = "urn:ietf:params:oauth:grant-type:token-exchange";
    let params = `grant_type=${encodeURIComponent(grantType)}&scope=${encodeURIComponent("unknown example update")}`;
    params += `&subject_token=${encodeURIComponent(token)}`;
    params += `&resource=${encodeURIComponent("https://localhost:9859/anything/backend")}`;
    const fromTokenType = encodeURIComponent(`urn:ietf:params:oauth:token-type:${fromType}`);
    const requestedTokenType = `urn:ietf:params:oauth:token-type:${toType}`;
    params += `&subject_token_type=${fromTokenType}`;
    params += `&requested_token_type=${encodeURIComponent(requestedTokenType)}`;

    const url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
    await cas.log(`Exchanging token ${token}`);
    await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, async (res) => {
        assert(res.data.access_token !== undefined);
        assert(res.data.token_type === "Bearer");
        assert(res.data.expires_in !== undefined);
        assert(res.data.issued_token_type === requestedTokenType);
        assert(res.data.scope === "update");

        if (toType === "jwt") {
            const jwt = res.data.access_token;
            const decoded = await cas.decodeJwt(jwt);
            assert(decoded.sub === "client");
            assert(decoded.iss === "https://localhost:8443/cas");
            assert(decoded.aud === "https://localhost:9859/anything/backend");
            assert(decoded.iat !== undefined);
            assert(decoded.jti !== undefined);
            assert(decoded.grant_type === "client_credentials");
            assert(decoded.client_id === "client");
        }
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

(async () => {
    const params = "grant_type=client_credentials&scope=read";
    const url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
    await cas.log(`Calling ${url}`);

    await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, async (res) => {
        await cas.log(res.data);
        assert(res.data.access_token !== undefined);
        assert(res.data.refresh_token !== undefined);

        await exchangeToken(res.data.access_token, "access_token", "access_token");
        await exchangeToken(res.data.access_token, "access_token", "jwt");

    }, (error) => {
        throw `Operation failed: ${error}`;
    });

})();
