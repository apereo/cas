const assert = require("assert");
const cas = require("../../cas.js");

async function executeRequest(clientId = "client", scope = "example", resource) {
    let params = "grant_type=client_credentials";
    if (scope !== "" && scope !== undefined) {
        params += `&scope=${encodeURIComponent(scope)}`;
    }
    if (resource !== undefined) {
        params += `&resource=${encodeURIComponent(resource)}`;
    }
    const url = `https://localhost:8443/cas/oauth2.0/token?${params}`;
    await cas.doPost(url, params, {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa(`${clientId}:secret`)}`
    }, async (res) => {
        cas.log(res.data);
        assert(res.data.access_token !== undefined);
        const decoded = await cas.decodeJwt(res.data.access_token, true);
        assert(decoded !== undefined);
        assert(decoded.payload["sub"] === clientId);
        const audience = Array.isArray(decoded.payload["aud"]) ? decoded.payload["aud"] : [decoded.payload["aud"]];
        assert.deepStrictEqual(audience, resource === undefined ? [clientId] : [clientId, resource]);
        assert(decoded.payload.client_id === clientId);
        assert(decoded.payload.grant_type === "client_credentials");
        assert(decoded.payload.username === clientId);
        assert(decoded.payload.email === "casuser@apereo.org");
        assert(decoded.payload.password === undefined);

    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

(async () => {
    await executeRequest("client", "example", "https://api.example.com/app/");
    await cas.separator();
    await executeRequest("client2", "");
})();
