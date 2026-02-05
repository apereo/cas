const cas = require("../../cas.js");
const path = require("path");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const har = await cas.startHar(page);
    
    await cas.gotoLogin(page);
    await cas.sleep(2000);

    await cas.click(page, "li #SAML2Client");
    await cas.waitForNavigation(page);

    await cas.loginWith(page, "user1", "password");
    await cas.sleep(2000);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");

    const clientId = "client";

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

    assert(idToken !== null && idToken !== undefined);
    const logoutRedirect = "https://localhost:9859/anything/redirect";
    let logoutUrl = "https://localhost:8443/cas/oidc/oidcLogout";
    logoutUrl += `?post_logout_redirect_uri=${logoutRedirect}`;
    logoutUrl += "&state=1234567890";
    logoutUrl += `&client_id=${clientId}`;
    logoutUrl += `&id_token_hint=${idToken}`;
    const response = await cas.goto(page, logoutUrl);
    await cas.sleep(5000);
    await cas.logPage(page);
    await cas.log(`${response.status()} ${response.statusText()}`);
    await cas.assertPageUrlStartsWith(page, logoutRedirect);

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.stopHar(har);
    await cas.closeBrowser(browser);
})();

