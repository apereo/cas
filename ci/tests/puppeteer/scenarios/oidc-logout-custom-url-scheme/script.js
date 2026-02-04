const assert = require("assert");
const cas = require("../../cas.js");

async function assertPageUrlDoesNotContain(page, url) {
    const result = await page.url();
    assert(!result.includes(url));
}

async function verifyLogoutWithIdTokenHint(clientId, redirectUrl, page) {
    let params = "grant_type=client_credentials&";
    params += `scope=${encodeURIComponent("openid")}`;
    const tokenUrl = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${tokenUrl} for client id ${clientId} to obtain an ID token...`);

    const idToken = await cas.doPost(tokenUrl, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa(`${clientId}:secret`)}`
    }, async (res) => {
        await cas.log(res.data.id_token);
        return res.data.id_token;
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
    
    let logoutUrl = "https://localhost:8443/cas/oidc/oidcLogout";
    logoutUrl += `?post_logout_redirect_uri=${redirectUrl}`;
    logoutUrl += "&state=1234567890";
    logoutUrl += `&client_id=${clientId}`;
    logoutUrl += `&id_token_hint=${idToken}`;

    // XXX Unable to handle custom URL schemes. This call causes a
    // net::ERR_ABORTED error, which results in response === null.
    const response = await cas.goto(page, logoutUrl, 1);
    await cas.sleep(1000);
    await cas.logPage(page);
    if (response !== null) {
        await cas.log(`${response.status()} ${response.statusText()}`);
    }

    // We can't assert anything positive about the resulting URL, but we can at
    // least assert that the redirect URL is NOT treated as a relative path.
    await assertPageUrlDoesNotContain(page, `/${redirectUrl}`);

    await cas.gotoLogout(page, "https://localhost:9859/anything/oidc&client_id=whatever");
    await cas.sleep(1000);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/logout");
}

(async () => {
    const casService = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    let response = await cas.gotoLogout(page, casService);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    await cas.assertPageUrl(page, casService);

    response = await cas.goto(page, await page.url());
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() === 200);
    await cas.assertPageUrl(page, casService);

    const customSchemeRedirect = "custom://localhost:9859/anything/oidc";
    await verifyLogoutWithIdTokenHint("client", customSchemeRedirect, page);

    const customIssuerService = "custom://localhost:9859/anything/customissuer";
    await verifyLogoutWithIdTokenHint("customclient", customIssuerService, page);
    await cas.closeBrowser(browser);
})();
