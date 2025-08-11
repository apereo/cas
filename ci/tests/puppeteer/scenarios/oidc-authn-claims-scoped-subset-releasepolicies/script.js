
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const url1 = "https://localhost:9859/anything/sample1";
    await cas.logg(`Trying with URL ${url1}`);

    const payload = await getPayload(page, url1, "client1", "secret1",
        encodeURIComponent("openid email"));
    const decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub === "CAS@EXAMPLE.ORG");
    assert(decoded.aud === "client1");
    assert(decoded["preferred_username"] === "CAS@EXAMPLE.ORG");
    assert(decoded["family_name"] === undefined);
    assert(decoded["given_name"] === undefined);
    assert(decoded["email"] === "cas@example.org");

    const profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${payload.access_token}`;
    await cas.log(`Calling user profile ${profileUrl}`);
    await cas.doPost(profileUrl, "", {
        "Content-Type": "application/json"
    }, (res) => {
        assert(res.data.id === "CAS@EXAMPLE.ORG");
        assert(res.data.sub === "CAS@EXAMPLE.ORG");
        assert(res.data.attributes["email"] === "cas@example.org");
        assert(res.data.attributes["given_name"] === undefined);
        assert(res.data.attributes["family_name"] === undefined);
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    await cas.closeBrowser(browser);
})();

async function getPayload(page, redirectUri, clientId, clientSecret, scopes) {
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=${scopes}&redirect_uri=${redirectUri}`;
    await cas.goto(page, url);
    await cas.log(`Authorize URL: ${page.url()}`);
    await cas.sleep(1000);
    
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(1000);
    }
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await cas.waitForNavigation(page);
    }

    const code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=${clientId}&client_secret=${clientSecret}&redirect_uri=${redirectUri}&code=${code}`;

    return cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
}
