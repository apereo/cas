const cas = require("../../cas.js");
const assert = require("assert");

async function sendRequest(url, clientid, clientsecret) {
    const headers = {
        "Content-Type": "application/x-www-form-urlencoded",
        "Authorization": `Basic ${btoa(`${clientid}:${clientsecret}`)}`
    };
    return cas.doPost(url, "", headers,
        async (res) => res.data,
        (error) => {
            throw `Operation failed: ${error}`;
        });
}

async function verifyCustomScopeForClientApp1() {
    const params = `grant_type=client_credentials&scope=${encodeURIComponent("openid Gordon")}`;
    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);
    const result = await sendRequest(url, "client", "secret");
    assert(result.id_token !== undefined);
    assert(result.access_token !== undefined);

    await cas.log("Decoding ID token");
    let decoded = await cas.decodeJwt(result.id_token);
    assert(decoded["uid"] !== undefined);
    assert(decoded["firstname"] !== undefined);
    assert(decoded["city"] === undefined);
    assert(decoded["lastname"] === undefined);

    await cas.log("Decoding access token");
    decoded = await cas.decodeJwt(result.access_token);
    assert(decoded["uid"] !== undefined);
    assert(decoded["firstname"] !== undefined);
    assert(decoded["city"] === undefined);
    assert(decoded["lastname"] === undefined);
}

async function verifyCustomScopeForClientApp2() {
    const params = `grant_type=client_credentials&scope=${encodeURIComponent("openid Gordon")}`;
    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);
    const result = await sendRequest(url, "client2", "secret2");
    assert(result.id_token !== undefined);
    assert(result.access_token !== undefined);

    await cas.log("Decoding ID token");
    let decoded = await cas.decodeJwt(result.id_token);
    assert(decoded["uid"] === undefined);
    assert(decoded["firstname"] === undefined);
    assert(decoded["city"] !== undefined);
    assert(decoded["lastname"] !== undefined);

    await cas.log("Decoding access token");
    decoded = await cas.decodeJwt(result.access_token);
    assert(decoded["uid"] === undefined);
    assert(decoded["firstname"] === undefined);
    assert(decoded["city"] !== undefined);
    assert(decoded["lastname"] !== undefined);
}

async function verifyCustomScopeForClientApp3() {
    const params = `grant_type=client_credentials&scope=${encodeURIComponent("openid Gordon")}`;
    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);
    const result = await sendRequest(url, "client3", "secret3");
    assert(result.id_token !== undefined);
    assert(result.access_token !== undefined);

    await cas.log("Decoding ID token");
    let decoded = await cas.decodeJwt(result.id_token);
    assert(decoded["uid"] !== undefined);
    assert(decoded["firstname"] !== undefined);
    assert(decoded["city"] !== undefined);
    assert(decoded["lastname"] !== undefined);

    await cas.log("Decoding access token");
    decoded = await cas.decodeJwt(result.access_token);
    assert(decoded["uid"] !== undefined);
    assert(decoded["firstname"] !== undefined);
    assert(decoded["city"] !== undefined);
    assert(decoded["lastname"] !== undefined);
}

(async () => {
    await verifyCustomScopeForClientApp1();
    await verifyCustomScopeForClientApp2();
    await verifyCustomScopeForClientApp3();
})();
