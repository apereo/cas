const assert = require("assert");
const cas = require("../../cas.js");

(async () => {

    let params = "grant_type=client_credentials&";
    params += "scope=openid";
    const urlOidc = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${urlOidc}`);

    await cas.doPost(urlOidc, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, async (res) => {

        await cas.log(res.data);
        assert(res.data.access_token !== null);

        await cas.log("Decoding JWT ID token...");
        const idTokenDecoded = await cas.decodeJwt(res.data.id_token);

        await cas.log("Decoding JWT access token...");
        const accessTokenDecoded = await cas.decodeJwt(res.data.access_token);

        assert(res.data.id_token !== undefined);
        assert(res.data.refresh_token !== undefined);
        assert(res.data.token_type !== undefined);
        assert(res.data.scope !== undefined);
        assert(idTokenDecoded.sub !== undefined);
        assert(idTokenDecoded.cn !== undefined);
        assert(idTokenDecoded.name !== undefined);
        assert(idTokenDecoded["preferred_username"] !== undefined);
        assert(idTokenDecoded["given-name"] !== undefined);
        assert(accessTokenDecoded.name !== undefined);
        assert(accessTokenDecoded.cn !== undefined);
        assert(accessTokenDecoded["given-name"] !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    const urlOAuth = `https://localhost:8443/cas/oauth2.0/accessToken?${params}`;
    await cas.log(`Calling ${urlOAuth}`);

    await cas.doPost(urlOAuth, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, async (res) => {

        await cas.log(res.data);
        assert(res.data.access_token !== null);

        await cas.log("Decoding JWT access token...");
        const accessTokenDecoded = await cas.decodeJwt(res.data.access_token);

        assert(res.data.id_token === undefined);
        assert(res.data.refresh_token !== undefined);
        assert(res.data.token_type !== undefined);
        assert(res.data.scope !== undefined);
        assert(accessTokenDecoded.name !== undefined);
        assert(accessTokenDecoded.cn !== undefined);
        assert(accessTokenDecoded["given-name"] !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
})();
