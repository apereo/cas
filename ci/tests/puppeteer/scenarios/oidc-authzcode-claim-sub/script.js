
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const url1 = "https://localhost:9859/anything/sample1";
    await cas.logg(`Trying with URL ${url1}`);
    let payload = await getPayload(page, url1, "client1", "secret1");
    let decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub === "CAS@EXAMPLE.ORG");
    assert(decoded["preferred_username"] === "CAS@EXAMPLE.ORG");

    const url2 = "https://localhost:9859/anything/sample2";
    await cas.logg(`Trying with URL ${url2}`);
    payload = await getPayload(page, url2, "client2", "secret2");
    decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub === "CASSSO");
    assert(decoded["preferred_username"] === "CASSSO");

    const url3 = "https://localhost:9859/anything/sample3";
    await cas.logg(`Trying with URL ${url3}`);
    payload = await getPayload(page, url3, "client3", "secret3");
    decoded = await cas.decodeJwt(payload.id_token);
    assert(decoded.sub === "ABCDEFG@APEREO.ORG");
    assert(decoded["preferred_username"] === "ABCDEFG@APEREO.ORG");

    await cas.logg(`Trying with URL ${url1} but without the openid scope`);
    payload = await getPayload(page, url1, "client1", "secret1", "profile email");
    assert(payload.id_token === undefined);
    
    await cas.closeBrowser(browser);
})();

async function getPayload(page, redirectUri, clientId, clientSecret, scopes = "openid profile email") {
    const encodedScopes = encodeURIComponent(scopes);
    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${clientId}&scope=${encodedScopes}&redirect_uri=${redirectUri}`;
    await cas.goto(page, url);
    await cas.logPage(page);
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
