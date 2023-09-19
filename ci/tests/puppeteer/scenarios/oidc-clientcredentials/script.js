const assert = require('assert');
const cas = require('../../cas.js');

async function sendRequest(url) {
    await cas.doPost(url, "", {
        'Content-Type': "application/json"
    }, async res => {
        const urlObj = new URL(url);

        await cas.log(res.data);
        assert(res.data.access_token !== null);

        await cas.log("Decoding JWT access token...");
        let accessToken = await cas.decodeJwt(res.data.access_token);
        assert(accessToken.sub === "casuser");
        assert(accessToken.name === "ApereoCAS");
        assert(accessToken["client_id"] === urlObj.searchParams.get("client_id"));
        assert(accessToken["gender"] === "Female");
        assert(accessToken["family_name"] === "Apereo");
        assert(accessToken["given_name"] === "CAS");
        assert(accessToken["organization"] === "ApereoFoundation");

        await cas.log("Decoding JWT ID token...");
        let idToken = await cas.decodeJwt(res.data.id_token);

        assert(res.data.id_token !== null);
        assert(res.data.refresh_token !== null);
        assert(res.data.token_type !== null);
        assert(res.data.scope.includes('MyCustomScope'));
        assert(res.data.scope.includes('profile'));
        assert(res.data.scope.includes('openid'));

        assert(idToken.sub === "casuser");
        assert(idToken["cn"] === undefined);
        assert(idToken.name === "ApereoCAS");
        assert(idToken["client_id"] === urlObj.searchParams.get("client_id"));
        assert(idToken["preferred_username"] === "casuser");
        assert(idToken["gender"] === "Female");
        assert(idToken["family_name"] === "Apereo");
        assert(idToken["given-name"] === undefined);
        assert(idToken["given_name"] === "CAS");
        assert(idToken["organization"] === "ApereoFoundation");
    }, error => {
        throw `Operation failed: ${error}`;
    });
}

async function verifyPasswordGrantType() {
    let params = "client_id=client&client_secret=secret&grant_type=password&username=casuser&password=P@SSw0rd&";
    params += `scope=${encodeURIComponent("openid MyCustomScope email profile eduPerson")}`;
    let url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);
    await sendRequest(url);
}

async function verifyClientCredentialsGrantType() {
    let params = "client_id=client2&client_secret=secret2&grant_type=client_credentials&";
    params += `scope=${encodeURIComponent("openid MyCustomScope email profile eduPerson")}`;
    let url = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${url}`);
    await sendRequest(url);
}

(async () => {
    await verifyPasswordGrantType();
    await verifyClientCredentialsGrantType();
})();
