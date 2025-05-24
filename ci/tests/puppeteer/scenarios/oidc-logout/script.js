const assert = require("assert");
const cas = require("../../cas.js");

async function verifyLogoutWithIdTokenHint(clientId, casService, page) {
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
    logoutUrl += `?post_logout_redirect_uri=${casService}`;
    logoutUrl += "&state=1234567890";
    logoutUrl += `&client_id=${clientId}`;
    logoutUrl += `&id_token_hint=${idToken}`;
    const response = await cas.goto(page, logoutUrl);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.log(`${response.status()} ${response.statusText()}`);

    const url = await page.url();
    assert(url.startsWith(casService));
    await cas.assertParameter(page, "state");
    await cas.assertParameter(page, "client_id");

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
    let url = await page.url();
    assert(url === casService);

    response = await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() === 200);
    url = await page.url();
    assert(url === casService);
    
    await verifyLogoutWithIdTokenHint("client", casService, page);
    
    const customIssuerService = "https://localhost:9859/anything/customissuer";
    await verifyLogoutWithIdTokenHint("customclient", customIssuerService, page);
    await browser.close();
})();
