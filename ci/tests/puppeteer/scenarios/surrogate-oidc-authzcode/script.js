const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const redirectUri = "https://localhost:9859/anything/sample1";
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?response_type=code"
        + `&client_id=client&scope=${encodeURIComponent("openid profile email")}`
        + `&redirect_uri=${redirectUri}&nonce=3d3a7457f9ad3&state=1735fd6c43c14`;
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.loginWith(page, "user3+casuser", "Mellon");
    await cas.sleep(3000);
    await cas.screenshot(page);
    await cas.logPage(page);
    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    await browser.close();

    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&scope=${encodeURIComponent("openid profile email")}`
        + `&client_id=client&client_secret=secret&redirect_uri=${redirectUri}&code=${code}`;
    const payload = await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json",
        "Accept": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
    assert(payload.access_token !== undefined);
    assert(payload.token_type !== undefined);
    assert(payload.expires_in !== undefined);
    assert(payload.scope !== undefined);

    const decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub === "user3");
    assert(decoded.client_id === "client");
    assert(decoded["preferred_username"] === "user3");
    assert(decoded["email"] === "casuser@example.org");
    assert(decoded["family_name"] === "Apereo");
    assert(decoded["name"] === "CAS");
    assert(decoded["surrogateEnabled"] === true);
    assert(decoded["surrogatePrincipal"] === "casuser");
    assert(decoded["surrogateUser"] === "user3");
    assert(decoded["txn"] !== undefined);
    assert(decoded["jti"] !== undefined);
    assert(decoded["sid"] !== undefined);
    assert(decoded["aud"] === "client");

    const profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${payload.access_token}`;
    await cas.log(`Calling user profile ${profileUrl}`);

    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data["service"] === redirectUri);
        assert(res.data["name"] === "CAS");
        assert(res.data["family_name"] === "Apereo");
        assert(res.data["email"] === "casuser@example.org");
        assert(res.data.id === "user3");
        assert(res.data.sub === "user3");
        assert(res.data.client_id === "client");
        assert(res.data["surrogateEnabled"] === true);
        assert(res.data["surrogatePrincipal"] === "casuser");
        assert(res.data["surrogateUser"] === "user3");
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

})();
