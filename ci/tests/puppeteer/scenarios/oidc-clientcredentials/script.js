const assert = require('assert');
const cas = require('../../cas.js');

async function sendRequest(url) {
    await cas.doPost(url, "", {
        'Content-Type': "application/json"
    }, async res => {
        const urlObj = new URL(url);

        console.log(res.data);
        assert(res.data.access_token !== null);

        console.log("Decoding JWT access token...");
        let accessToken = await cas.decodeJwt(res.data.access_token);
        assert(accessToken.sub === "casuser");
        assert(accessToken.name === "ApereoCAS");
        assert(accessToken["client_id"] === urlObj.searchParams.get("client_id"));
        assert(accessToken["preferred_username"] === "casuser");
        assert(accessToken["gender"] === "Female");
        assert(accessToken["family_name"] === "Apereo");
        assert(accessToken["given_name"] === "CAS");

        console.log("Decoding JWT ID token...");
        let decoded = await cas.decodeJwt(res.data.id_token);

        assert(res.data.id_token !== null);
        assert(res.data.refresh_token !== null);
        assert(res.data.token_type !== null);
        assert(res.data.scope === 'MyCustomScope openid profile');

        assert(decoded.sub === "casuser");
        assert(decoded["cn"] === undefined);
        assert(decoded.name === "ApereoCAS");
        assert(decoded["client_id"] === urlObj.searchParams.get("client_id"));
        assert(decoded["preferred_username"] === "casuser");
        assert(decoded["gender"] === "Female");
        assert(decoded["family_name"] === "Apereo");
        assert(decoded["given-name"] === undefined);
        assert(decoded["given_name"] === "CAS");
    }, error => {
        throw `Operation failed: ${error}`;
    });
}

async function verifyPasswordGrantType() {
    let params = "client_id=client&client_secret=secret&grant_type=password&username=casuser&password=P@SSw0rd&";
    params += `scope=${encodeURIComponent("openid MyCustomScope email profile")}`;
    let url = `https://localhost:8443/cas/oidc/token?${params}`;
    console.log(`Calling ${url}`);
    await sendRequest(url);
}

async function verifyClientCredentialsGrantType() {
    let params = "client_id=client2&client_secret=secret2&grant_type=client_credentials&";
    params += `scope=${encodeURIComponent("openid MyCustomScope email profile")}`;
    let url = `https://localhost:8443/cas/oidc/token?${params}`;
    console.log(`Calling ${url}`);
    await sendRequest(url);
}

(async () => {
    await verifyPasswordGrantType();
    await verifyClientCredentialsGrantType();
})();
