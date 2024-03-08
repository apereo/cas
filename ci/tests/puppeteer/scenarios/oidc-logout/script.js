const assert = require("assert");
const cas = require("../../cas.js");

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

    let logoutUrl = "https://localhost:8443/cas/oidc/oidcLogout";
    logoutUrl += `?post_logout_redirect_uri=${casService}`;
    logoutUrl += "&state=1234567890";

    response = await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() === 200);
    url = await page.url();
    assert(url === casService);

    let params = "grant_type=client_credentials&";
    params += `scope=${encodeURIComponent("openid")}`;
    const tokenUrl = `https://localhost:8443/cas/oidc/token?${params}`;
    await cas.log(`Calling ${tokenUrl}`);

    const idToken = await cas.doPost(tokenUrl, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa("client:secret")}`
    }, async (res) => {
        await cas.log(res.data.id_token);
        return res.data.id_token;
    }, (error) => {
        throw `Operation failed: ${error}`;
    });

    logoutUrl += `&id_token_hint=${idToken}`;
    response = await cas.goto(page, logoutUrl);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.log(`${response.status()} ${response.statusText()}`);

    url = await page.url();
    assert(url.startsWith(casService));
    await cas.assertParameter(page, "state");
    await cas.assertParameter(page, "client_id");

    await cas.gotoLogout(page, "https://localhost:9859/anything/oidc&client_id=whatever");
    await cas.sleep(1000);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/logout");

    await browser.close();
})();
