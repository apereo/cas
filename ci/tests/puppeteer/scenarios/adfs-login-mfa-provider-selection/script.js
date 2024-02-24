const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const service = "https://localhost:9859/anything/cas";
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.log(`Navigating to ${service}`);
    await cas.gotoLogin(page, service);
    await cas.waitForTimeout(page);
    await cas.click(page, "div .idp span");
    await cas.waitForTimeout(page, 4000);
    await cas.type(page, "#userNameInput", process.env.ADFS_USERNAME, true);
    await cas.type(page, "#passwordInput", process.env.ADFS_PASSWORD, true);
    await cas.waitForTimeout(page);
    await cas.submitForm(page, "#loginForm");
    await cas.waitForTimeout(page, 4000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#content h2", "Multifactor Authentication Provider Selection");
    await cas.assertVisibility(page, "#mfa-yubikey");
    await cas.assertVisibility(page, "#mfa-simple");
    await browser.close();
})();
