const assert = require("assert");
const cas = require("../../cas.js");

const CLIENT_ID = "client";

async function executeRequest(clientSecret = "secret") {
    const params = `client_id=${CLIENT_ID}&client_secret=${clientSecret}&grant_type=client_credentials&`;
    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);

    await cas.doPost(url, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa(`${CLIENT_ID}:${clientSecret}`)}`
    }, async (res) => {
        await cas.log(res.data);
        assert(res.data.access_token !== undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

(async () => {
    await executeRequest();
    await cas.separator();
    await cas.log("Rotate all expired client secrets...");
    let response = JSON.parse(await cas.doRequest(`https://localhost:8443/cas/actuator/oauthClientSecrets/${CLIENT_ID}?expiredOnly=true`, "POST",
        {"Content-Type": "application/json"}, 200));
    await cas.log(response);
    let expiration = response.clientSecrets[0].expiration;
    assert(expiration === undefined);
    assert(response.clientSecrets[0].value === "secret");
    await cas.separator();
    await cas.log("Rotate all client secrets...");
    response = JSON.parse(await cas.doRequest(`https://localhost:8443/cas/actuator/oauthClientSecrets/${CLIENT_ID}?expireIn=P1D`, "POST",
        {"Content-Type": "application/json"}, 200));
    await cas.log(response);
    assert(response.clientSecrets[0].value !== "secret");
    expiration = new Date(response.clientSecrets[0].expiration * 1000);
    await cas.log(`Secret will expire at ${expiration.toISOString()}`);

    const now = new Date();
    const tomorrowUtc = new Date(Date.UTC(
        now.getUTCFullYear(),
        now.getUTCMonth(),
        now.getUTCDate() + 1
    ));

    const expirationUtcDay = new Date(Date.UTC(
        expiration.getUTCFullYear(),
        expiration.getUTCMonth(),
        expiration.getUTCDate()
    ));
    await cas.log(`Expiration ${expirationUtcDay}, Tomorrow ${tomorrowUtc}`);
    assert.deepStrictEqual(expirationUtcDay, tomorrowUtc);
    await cas.separator();

    await executeRequest(response.clientSecrets[0].value);
    await cas.separator();
})();
