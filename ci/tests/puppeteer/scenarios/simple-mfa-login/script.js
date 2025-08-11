
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-simple", "en");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#token");
    await cas.attributeValue(page, "html", "lang", "en");

    const code = await cas.extractFromEmail(browser);

    await page.bringToFront();
    await cas.attributeValue(page, "html", "lang", "en");
    await cas.type(page, "#token", "unknownCode");
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);
    await cas.assertTextContentStartsWith(page, "div .banner-danger p", "Multifactor authentication attempt has failed");

    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(3000);

    await cas.submitForm(page, "#registerform");
    await cas.sleep(3000);

    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);

    await cas.closeBrowser(browser);
})();
