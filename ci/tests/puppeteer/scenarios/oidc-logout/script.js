const assert = require("assert");
const cas = require("../../cas.js");
const crypto = require("crypto");

const oidcLogoutEndpoint = "https://localhost:8443/cas/oidc/oidcLogout";

async function fetchIdToken(clientId) {
    const tokenUrl = `https://localhost:8443/cas/oidc/token?grant_type=client_credentials&scope=${encodeURIComponent("openid")}`;
    return cas.doPost(tokenUrl, "", {
        "Content-Type": "application/json",
        "Authorization": `Basic ${btoa(`${clientId}:secret`)}`
    }, (res) => {
        assert(res.data.id_token !== undefined);
        return res.data.id_token;
    }, (error) => {
        throw `Operation failed: ${error}`;
    });
}

async function verifyLogoutRedirectValidation(page) {
    const idToken = await fetchIdToken("client");

    await cas.log("A client_id that does not match the client of the ID token hint is refused");
    let response = await cas.goto(page, `${oidcLogoutEndpoint}?post_logout_redirect_uri=https://localhost:9859/anything/customissuer`
        + `&client_id=customclient&id_token_hint=${idToken}`);
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() === 400);
    await cas.assertPageUrlHost(page, "localhost:8443");

    await cas.log("An ID token hint that CAS did not sign is refused, even with a registered redirect URI");
    const foreignKey = crypto.generateKeyPairSync("rsa", {modulusLength: 2048})
        .privateKey.export({type: "pkcs8", format: "pem"});
    const now = Math.floor(Date.now() / 1000);
    const forgedIdToken = await cas.createJwt({
        "iss": "https://localhost:8443/cas/oidc",
        "aud": "client",
        "sub": "casuser",
        "client_id": "client",
        "iat": now,
        "exp": now + 300
    }, foreignKey);
    response = await cas.goto(page, `${oidcLogoutEndpoint}?post_logout_redirect_uri=https://localhost:9859/anything/cas`
        + `&state=1234567890&id_token_hint=${forgedIdToken}`);
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() === 400);
    await cas.assertPageUrlHost(page, "localhost:8443");

    await cas.log("An ID token hint that is not a JWT is refused as a bad request");
    response = await cas.goto(page, `${oidcLogoutEndpoint}?post_logout_redirect_uri=https://localhost:9859/anything/cas`
        + "&state=1234567890&id_token_hint=not-a-jwt");
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() === 400);
    await cas.assertPageUrlHost(page, "localhost:8443");

    await cas.log("A post_logout_redirect_uri that matches no registered service ends the session without redirecting");
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);
    response = await cas.goto(page, `${oidcLogoutEndpoint}?post_logout_redirect_uri=https://localhost:9859/anything/rogue`
        + `&state=1234567890&client_id=client&id_token_hint=${idToken}`);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.log(`${response.status()} ${response.statusText()}`);
    await cas.assertPageUrlHost(page, "localhost:8443");
    await cas.assertPageUrlStartsWith(page, oidcLogoutEndpoint);
    await cas.assertInnerTextStartsWith(page, "#logoutMessage", "You have successfully logged out");
    await cas.assertCookie(page, false);
}

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

    if (casService.startsWith("custom://")) {
        const customSchemeRequests = [];
        page.on("request", (req) => {
            if (req.url().startsWith("custom://")) {
                customSchemeRequests.push(req.url());
            }
        });
        await cas.goto(page, logoutUrl, 1);
        await cas.sleep(1000);
       
        assert(customSchemeRequests.length > 0);
        const finalUrl = customSchemeRequests[0];
        await cas.log(finalUrl);
        assert(finalUrl.startsWith("custom://localhost:9859/anything/customissuer"));
        assert(finalUrl.includes("state=1234567890"));
        assert(finalUrl.includes(`client_id=${clientId}`));
    } else {
        const response = await cas.goto(page, logoutUrl);
        await cas.sleep(1000);
        await cas.logPage(page);
        await cas.log(`${response.status()} ${response.statusText()}`);

        await cas.assertPageUrlStartsWith(page, casService);
        await cas.assertParameter(page, "state");
        await cas.assertParameter(page, "client_id");
    }

    await cas.gotoLogout(page, "https://localhost:9859/anything/oidc&client_id=whatever");
    await cas.sleep(1000);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/logout");
}

(async () => {
    const casService = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);
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
    
    await verifyLogoutWithIdTokenHint("client", casService, page);

    const customIssuerService = "https://localhost:9859/anything/customissuer";
    await verifyLogoutWithIdTokenHint("customclient", customIssuerService, page);

    const customScheme = "custom://localhost:9859/anything/customissuer";
    await verifyLogoutWithIdTokenHint("customclient", customScheme, page);

    await verifyLogoutRedirectValidation(await cas.newPage(context, true));

    await context.close();
    await cas.closeBrowser(browser);
})();
