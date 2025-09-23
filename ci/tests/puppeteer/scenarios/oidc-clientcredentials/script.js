const assert = require("assert");
const cas = require("../../cas.js");

async function sendRequest(url, clientid, clientsecret) {
    let headers;
    if (clientsecret !== "") {
        headers = {
            "Content-Type": "application/json",
            "Authorization": `Basic ${btoa(`${clientid}:${clientsecret}`)}`
        };
    } else {
        headers = {
            "Content-Type": "application/json"
        };
    }
    await cas.doPost(url, "", headers, async (res) => {
        await cas.log(res.data);
        assert(res.data.access_token !== undefined);

        await cas.log("Decoding JWT access token...");
        const accessToken = await cas.decodeJwt(res.data.access_token);
        assert(accessToken.sub === "casuser");
        assert(accessToken.name === "ApereoCAS");
        assert(accessToken["aud"] === clientid);
        assert(accessToken["gender"] === "Female");
        assert(accessToken["family_name"] === "Apereo");
        assert(accessToken["given_name"] === "CAS");
        assert(accessToken["organization"] === "ApereoFoundation");

        await cas.log("Decoding JWT ID token...");
        const idToken = await cas.decodeJwt(res.data.id_token);

        assert(res.data.id_token !== undefined);
        assert(res.data.refresh_token !== undefined);
        assert(res.data.token_type !== undefined);
        assert(res.data.scope.includes("MyCustomScope"));
        assert(res.data.scope.includes("profile"));
        assert(res.data.scope.includes("openid"));

        assert(idToken.sub === "casuser");
        assert(idToken["cn"] === undefined);
        assert(idToken.name === "ApereoCAS");
        assert(idToken["aud"] === clientid);
        assert(idToken["preferred_username"] === "casuser");
        assert(idToken["gender"] === "Female");
        assert(idToken["family_name"] === "Apereo");
        assert(idToken["given-name"] === undefined);
        assert(idToken["given_name"] === "CAS");
        assert(idToken["organization"] === "ApereoFoundation");
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

async function verifyPasswordGrantType() {
    let params = "client_id=client&client_secret=secret&grant_type=password&username=casuser&password=P@SSw0rd&";
    params += `scope=${encodeURIComponent("openid MyCustomScope email profile eduPerson")}`;
    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);
    await sendRequest(url, "client", "");
}

async function verifyClientCredentialsGrantType() {
    let params = "grant_type=client_credentials&";
    params += `scope=${encodeURIComponent("openid MyCustomScope email profile eduPerson")}`;
    const url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);
    await sendRequest(url, "client2", "secret2");
}

(async () => {
    await verifyPasswordGrantType();
    await verifyClientCredentialsGrantType();
})();
