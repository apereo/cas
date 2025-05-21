const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    let service = {
        "redirect_uris": ["https://apereo.github.io"],
        "client_name": "Apereo Blog",
        "contacts": ["cas@example.org"],
        "grant_types": ["client_credentials"]
    };
    let body = JSON.stringify(service, undefined, 2);
    await cas.log(`Sending ${body}`);
    let result = await cas.doRequest("https://localhost:8443/cas/oidc/register", "POST",
        {
            "Content-Length": body.length,
            "Content-Type": "application/json"
        }, 201, body);
    assert(result !== null);
    const entity = JSON.parse(result.toString());
    await cas.log(entity);
    assert(entity.client_id !== undefined);
    assert(entity.client_secret !== undefined);
    await cas.log("Using registration entry to get a token...");
    await executeRequest(entity.client_id, entity.client_secret, false);
    await cas.sleep(11000);
    await cas.log("Reusing a now-expired registration entry to get a token, which must fail...");
    await executeRequest(entity.client_id, entity.client_secret, true);

    await cas.log("Updating registration entity to renew client secret");
    await cas.log("==================================");
    service = {
        "redirect_uris": ["https://apereo.github.io"],
        "client_name": "Apereo Blog Updated Now",
        "contacts": ["cas@example.org", "new@example.org"],
        "grant_types": ["client_credentials"]
    };
    body = JSON.stringify(service, undefined, 2);
    await cas.log(`Sending ${body}`);

    result = await cas.doRequest(entity.registration_client_uri, "PATCH", {
        "Authorization": `Bearer ${entity.registration_access_token}`,
        "Content-Length": body.length,
        "Content-Type": "application/json"
    }, 200, body);
    const updatedEntity = JSON.parse(result.toString());
    await cas.log(updatedEntity);
    assert(entity.client_secret !== updatedEntity.client_secret);
    assert(updatedEntity.client_name === service.client_name);
    assert(updatedEntity.contacts.length === service.contacts.length);
    assert(entity.client_id === updatedEntity.client_id);
})();

async function executeRequest(clientId, clientSecret, expectFailure) {
    const params = "&grant_type=client_credentials&scope=openid";
    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);

    await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa(`${clientId}:${clientSecret}`)}`
    }, async (res) => cas.log(res.data), (error) => {
        if (expectFailure) {
            cas.logr(`Operation has correctly failed with status: ${error.response.status}`);
        } else {
            throw `Operation failed: ${error}`;
        }
    });
}
