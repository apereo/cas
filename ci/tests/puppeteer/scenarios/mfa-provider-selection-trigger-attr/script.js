const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    for (let i = 0; i < 2; i++) {
        const browser = await puppeteer.launch(cas.browserOptions());
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page, "https://apereo.github.io");
        await cas.loginWith(page);
        await page.waitForTimeout(2000);
        await cas.assertVisibility(page, "#mfa-gauth");
        await cas.assertVisibility(page, "#mfa-webauthn");

        await cas.log("Selecting mfa-gauth");
        await cas.submitForm(page, "#mfa-gauth > form[name=fm-mfa-gauth]");
        await page.waitForTimeout(1000);
        await cas.assertVisibility(page, "#imageQRCode");
        await cas.assertVisibility(page, "#confirm");

        await cas.logb("Having selected a provider, future attempts should remember it");
        await cas.gotoLogin(page, "https://apereo.github.io");
        await cas.loginWith(page);
        await page.waitForTimeout(1000);
        await cas.assertVisibility(page, "#imageQRCode");
        await cas.assertVisibility(page, "#confirm");
        await browser.close();
    }
})();
