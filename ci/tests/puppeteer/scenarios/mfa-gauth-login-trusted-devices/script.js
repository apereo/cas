const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
    await cas.loginWith(page);
    await cas.waitForTimeout(page);

    await cas.log("Using scratch code to login...");
    await cas.type(page,"#token", "83766843");
    await cas.submitForm(page, "#fm1");

    await cas.innerText(page, "#deviceName");
    await cas.type(page, "#deviceName", "My Trusted Device");

    await cas.waitForTimeout(page);

    await cas.assertInvisibility(page, "#expiration");
    await cas.assertVisibility(page, "#timeUnit");
    await cas.submitForm(page, "#registerform");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");

    await browser.close();
})();
