const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    let service = {
        "redirect_uris": ["https://apereo.github.io"],
        "client_name": "Apereo Blog",
        "contacts": ["cas@example.org"],
        "grant_types": ["client_credentials"],
    };
    let body = JSON.stringify(service, undefined, 2);
    console.log(`Sending ${body}`);
    let result = await cas.doRequest("https://localhost:8443/cas/oidc/register", "POST",
        {
            'Content-Length': body.length,
            'Content-Type': 'application/json'
        }, 201, body);
    assert(result !== null);
    let entity = JSON.parse(result.toString());
    console.log(entity);
    assert(entity.client_id !== null);
    assert(entity.client_secret !== null);

    console.log("Using registration entry to get a token...");
    await executeRequest(entity.client_id, entity.client_secret, false);
    await cas.sleep(5000);
    console.log("Re-using a now-expired registration entry to get a token, which must fail...");
    await executeRequest(entity.client_id, entity.client_secret, true);

    console.log("Updating registration entity to renew client secret");
    console.log("==================================");
    service = {
        "redirect_uris": ["https://apereo.github.io"],
        "client_name": "Apereo Blog Updated Now",
        "contacts": ["cas@example.org", "new@example.org"],
        "grant_types": ["client_credentials"],
    };
    body = JSON.stringify(service, undefined, 2);
    console.log(`Sending ${body}`);

    result = await cas.doRequest(entity.registration_client_uri, "PATCH", {
        "Authorization": `Bearer ${entity.registration_access_token}`,
        'Content-Length': body.length,
        'Content-Type': 'application/json'
    }, 200, body);
    let updatedEntity = JSON.parse(result.toString());
    console.log(updatedEntity);
    assert(entity.client_secret !== updatedEntity.client_secret);
    assert(updatedEntity.client_name === service.client_name);
    assert(updatedEntity.contacts.length === service.contacts.length);
    assert(entity.client_id === updatedEntity.client_id);
})();


async function executeRequest(clientId, clientSecret, expectFailure) {
    let params = `client_id=${clientId}&client_secret=${clientSecret}`;
    params += "&grant_type=client_credentials&scope=openid";
    let url = `https://localhost:8443/cas/oidc/token?${params}`;
    console.log(`Calling ${url}`);

    await cas.doPost(url, "", {
        'Content-Type': "application/json"
    }, async res => {
        console.log(res.data);
    }, error => {
        if (!expectFailure) {
            throw `Operation failed: ${error}`;
        } else {
            cas.logr(`Operation has correctly failed with status: ${error.response.status}`);
        }
    });
}
