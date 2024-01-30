const assert = require("assert");
const cas = require("../../cas.js");

(async () => {

    let params = "grant_type=client_credentials&";
    params += "scope=openid";

    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);

    await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, async (res) => {

        await cas.log(res.data);
        assert(res.data.access_token !== undefined);

        await cas.log("Decoding JWT access token...");
        const decoded = await cas.decodeJwt(res.data.id_token);

        assert(res.data.id_token !== undefined);
        assert(res.data.refresh_token !== undefined);
        assert(res.data.token_type !== undefined);
        assert(res.data.scope !== undefined);
        assert(decoded.sub !== undefined);
        assert(decoded.cn !== undefined);
        assert(decoded.name !== undefined);
        assert(decoded["preferred_username"] !== undefined);
        assert(decoded["given-name"] !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
})();
