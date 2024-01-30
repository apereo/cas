const assert = require("assert");
const cas = require("../../cas.js");

(async () => {

    let params = "client_id=client&";
    params += "client_secret=secret&";
    params += "grant_type=password&";
    params += "username=casuser&";
    params += "password=Mellon&";
    params += "scope=openid%20MyCustomScope%20email";

    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);

    await cas.doPost(url, "", {
        "Content-Type": "application/json"
    }, async (res) => {

        await cas.log(res.data);
        assert(res.data.access_token !== undefined);

        await cas.log("Decoding JWT access token...");
        await cas.decodeJwt(res.data.access_token);

        await cas.log("Decoding JWT ID token...");
        const decoded = await cas.decodeJwt(res.data.id_token);

        assert(res.data.id_token !== undefined);
        assert(res.data.refresh_token !== undefined);
        assert(res.data.token_type !== undefined);
        assert(res.data.scope === "MyCustomScope openid");
        
        assert(decoded.sub === "casuser");
        assert(decoded["cn"] === undefined);
        assert(decoded.name === "CAS");
        assert(decoded["client_id"] === "client");
        assert(decoded["preferred_username"] === "casuser");
        assert(decoded["gender"] === "Female");
        assert(decoded["given-name"] === undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
})();
