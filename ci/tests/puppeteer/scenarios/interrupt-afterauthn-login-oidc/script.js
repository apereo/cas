const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const redirectUri = "https://localhost:9859/anything/sample1";
    await cas.logg(`Trying with URL ${redirectUri}`);

    const url = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=${encodeURIComponent("openid profile")}&redirect_uri=${redirectUri}`;
    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.waitForTimeout(page, 1000);

    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.waitForTimeout(page, 1000);
    }
    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }

    await cas.screenshot(page);
    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await cas.waitForTimeout(page2, 1000);
    await cas.click(page2, "table tbody td a");
    await cas.waitForTimeout(page2, 1000);
    let code = await cas.textContent(page2, "div[name=bodyPlainText] .well");
    await page2.close();

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.waitForTimeout(page, 2000);

    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertCookie(page, false);
    await cas.assertTextContentStartsWith(page, "#interruptMessage", "We interrupted your login");
    await cas.assertVisibility(page, "#interruptLinks");
    await cas.assertVisibility(page, "#attributesTable");
    await cas.assertVisibility(page, "#field1");
    await cas.assertVisibility(page, "#field1-value");
    await cas.assertVisibility(page, "#field2");
    await cas.assertVisibility(page, "#field2-value");
    await cas.submitForm(page, "#fm1");
    await cas.waitForTimeout(page, 3000);
    await cas.screenshot(page);

    code = await cas.assertParameter(page, "code");
    await cas.log(`Current code is ${code}`);

    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code"
        + `&client_id=client&client_secret=secret&redirect_uri=${redirectUri}&code=${code}`;
    const accessToken = await cas.doPost(accessTokenUrl, "", {
        "Content-Type": "application/json"
    }, (res) => res.data, (error) => {
        throw `Operation failed to obtain access token: ${error}`;
    });
    const decoded = await cas.decodeJwt(accessToken.id_token);
    assert(decoded.sub !== undefined);
    assert(decoded.aud === "client");
    assert(decoded.client_id !== undefined);
    assert(decoded.preferred_username === "casuser@example.org");
    assert(decoded.family_name === "CAS");
    assert(decoded.given_name === "Apereo");
    assert(decoded.iss === "https://localhost:8443/cas/oidc");
    assert(decoded.acr === "mfa-simple");
    assert(decoded.amr.includes("CasSimpleMultifactorAuthenticationHandler"));
    assert(decoded.amr.includes("Static Credentials"));
    await browser.close();
})();
