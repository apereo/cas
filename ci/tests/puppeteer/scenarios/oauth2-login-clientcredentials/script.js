const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    let params = "scope=example&";
    params += "grant_type=client_credentials&";
    const url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
    await cas.doPost(url, params, {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, (res) => {
        cas.log(res.data);
        assert(res.data.access_token !== undefined);
        cas.decodeJwt(res.data.access_token, true).then((decoded) => {
            assert(decoded !== undefined);
            assert(decoded.payload["sub"] === "client");
            assert(decoded.payload["aud"] === "client");
            assert(decoded.payload.client_id === "client");
            assert(decoded.payload.grant_type === "client_credentials");
            assert(decoded.payload.username === "client");
            assert(decoded.payload.email === "casuser@apereo.org");
            assert(decoded.payload.password === undefined);
        });

    }, (error) => {
        throw `Operation failed: ${error}`;
    });
})();
