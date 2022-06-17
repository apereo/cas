const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/authorize?response_type=code"
        + "&client_id=client&scope=openid%20profile%20email&"
        + "redirect_uri=https://apereo.github.io";

    await page.goto(url);
    await page.waitForTimeout(1000)
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(30000)

    if (await cas.isVisible(page, "#allow")) {
        await cas.click(page, "#allow");
        await page.waitForNavigation();
    }
    
    let code = await cas.assertParameter(page, "code");
    console.log("Current code is " + code);
    const accessTokenUrl = "https://localhost:8443/cas/oidc/token?grant_type=authorization_code&client_id=client"
     + "&client_secret=secret&redirect_uri=https://apereo.github.io&code=" + code;
    await page.goto(accessTokenUrl);
    await page.waitForTimeout(1000)
    let content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    console.log(payload);
    assert(payload.access_token != null);
    assert(payload.token_type != null);
    assert(payload.expires_in != null);
    assert(payload.scope != null);

    let profileUrl = `https://localhost:8443/cas/oidc/profile?access_token=${payload.access_token}`;
    console.log(`Calling user profile ${profileUrl}`);
    
    await cas.doPost(profileUrl, "", {
        'Content-Type': "application/json"
    }, function (res) {
        console.log(res.data);
    }, function (error) {
        throw `Operation failed: ${error}`;
    });
    
    await browser.close();
})();
