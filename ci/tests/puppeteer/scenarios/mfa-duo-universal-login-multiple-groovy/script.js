const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await login(page, "https://localhost:9859/anything/sample", "mfa-duo");
    await cas.waitForTimeout(page, 2000);

    await login(page, "https://localhost:9859/anything/open", "mfa-duo-alt");
    await cas.waitForTimeout(page, 2000);

    await browser.close();
})();

async function login(page, service, providerId) {
    await cas.gotoLogout(page);
    await cas.waitForTimeout(page, 1000);
    await cas.assertCookie(page, false);
    await cas.log(`Trying with service ${service}`);
    await cas.gotoLogin(page, service);
    await cas.waitForTimeout(page, 2000);
    await cas.loginWith(page, "duobypass", "Mellon");
    await cas.waitForTimeout(page, 4000);
    await cas.screenshot(page);

    await cas.gotoLogin(page);
    await cas.screenshot(page);
    await cas.waitForElement(page, "#content");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", providerId);
    await cas.screenshot(page);
}
