const assert = require("assert");
const cas = require("../../cas.js");

async function exchangeToken(token, fromType, toType) {
    const grantType = "urn:ietf:params:oauth:grant-type:token-exchange";
    let params = `grant_type=${encodeURIComponent(grantType)}&scope=${encodeURIComponent("openid profile address phone")}`;
    params += `&subject_token=${encodeURIComponent(token)}`;
    params += `&resource=${encodeURIComponent("https://localhost:9859/anything/backend")}`;
    const fromTokenType = encodeURIComponent(`urn:ietf:params:oauth:token-type:${fromType}`);
    const requestedTokenType = `urn:ietf:params:oauth:token-type:${toType}`;
    params += `&subject_token_type=${fromTokenType}`;
    params += `&requested_token_type=${encodeURIComponent(requestedTokenType)}`;

    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Exchanging token ${token}`);
    await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, async (payload) => {
        switch (toType) {
        case "access_token":
        case "jwt":
            assert(payload.data.access_token !== undefined);
            assert(payload.data.token_type === "Bearer");
            assert(payload.data.expires_in !== undefined);
            assert(payload.data.issued_token_type === requestedTokenType);
            assert(payload.data.scope === "openid profile");
            break;
        case "id_token":
            assert(payload.data.access_token === undefined);
            assert(payload.data.token_type === undefined);
            assert(payload.data.expires_in === undefined);
            assert(payload.data.id_token !== undefined);
            assert(payload.data.issued_token_type === requestedTokenType);

            await cas.doGet("https://localhost:8443/cas/oidc/jwks",
                (keys) => {
                    assert(keys.status === 200);
                    assert(keys.data.keys[0]["kid"] !== undefined);
                    cas.log(`Using key identifier ${keys.data.keys[0]["kid"]}`);
                    cas.verifyJwtWithJwk(payload.data.id_token, keys.data.keys[0], "RS256").then((verified) => {
                        cas.log(verified);
                        assert(verified.payload.sub === "client");
                        assert(verified.payload.aud === "client");
                        assert(verified.payload.iss === "https://localhost:8443/cas/oidc");
                        assert(verified.payload.state === undefined);
                        assert(verified.payload.nonce === undefined);
                        assert(verified.payload.iat !== undefined);
                        assert(verified.payload.jti !== undefined);
                        assert(verified.payload.exp !== undefined);
                        assert(verified.payload.preferred_username === "client");
                        assert(verified.payload.name === "CAS");
                        assert(verified.payload.gender === "female");
                    });
                },
                (error) => {
                    throw error;
                });

            break;
        }
        if (toType === "jwt") {
            const jwt = payload.data.access_token;
            const decoded = await cas.decodeJwt(jwt);
            assert(decoded.sub === "client");
            assert(decoded.iss === "https://localhost:8443/cas/oidc");
            assert(decoded.aud === "https://localhost:9859/anything/backend");
            assert(decoded.iat !== undefined);
            assert(decoded.jti !== undefined);
        }
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

(async () => {
    const params = "grant_type=client_credentials&scope=openid";
    const url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
    await cas.log(`Calling ${url}`);

    await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, async (res) => {
        assert(res.data.access_token !== undefined);
        assert(res.data.refresh_token !== undefined);

        await exchangeToken(res.data.access_token, "access_token", "access_token");
        await exchangeToken(res.data.access_token, "access_token", "jwt");
        await exchangeToken(res.data.access_token, "access_token", "id_token");

    }, (error) => {
        throw `Operation failed: ${error}`;
    });

})();
