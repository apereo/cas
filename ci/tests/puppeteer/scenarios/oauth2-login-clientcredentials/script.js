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

async function executeRequestForUnknownResource() {
    const params = `grant_type=client_credentials&scope=example&resource=${encodeURIComponent("https://unknown.example.com/")}`;
    await cas.doPost(`https://localhost:8443/cas/oauth2.0/token?${params}`, params, {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, (res) => {
        throw `Operation should have failed: ${res.data}`;
    }, (error) => {
        assert(error.response.status === 400);
        assert(error.response.data.error === "invalid_target");
    });
}

async function verifyClientSecret(clientId, clientSecret, expectedStatus) {
    await cas.log(`Client ${clientId} authenticates with secret ${clientSecret}; expecting status ${expectedStatus}`);
    await cas.doPost("https://localhost:8443/cas/oauth2.0/token?grant_type=client_credentials", "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa(`${clientId}:${clientSecret}`)}`
    }, (res) => {
        assert(expectedStatus === 200, `Client secret ${clientSecret} of ${clientId} should be refused but produced: ${JSON.stringify(res.data)}`);
        assert(res.data.access_token !== undefined);
    }, (error) => {
        assert(error.response !== undefined, `No response: ${error}`);
        cas.log(`Status: ${error.response.status}`);
        assert(error.response.status === expectedStatus);
    });
}

(async () => {
    await executeRequest("client", "example", "https://api.example.com/app/");
    await cas.separator();
    await executeRequest("client2", "");
    await cas.separator();
    await executeRequestForUnknownResource();
    await cas.separator();

    await cas.log("An expired client secret is refused while the current one is accepted");
    await verifyClientSecret("rotatedclient", "oldsecret", 401);
    await verifyClientSecret("rotatedclient", "newsecret", 200);
    await cas.log("A client whose secrets have all expired is refused and is not taken for a public client");
    await verifyClientSecret("expiredclient", "secret", 401);
    await verifyClientSecret("expiredclient", "anything", 401);
})();
