
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-simple", "en");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#token");
    await cas.attributeValue(page, "html", "lang", "en");

    let code = await cas.extractFromEmail(browser);

    const page2 = await browser.newPage();
    await cas.gotoLoginWithAuthnMethod(page2, undefined, "mfa-simple", "en");
    await cas.loginWith(page2, "testcas", "password");
    await cas.sleep(2000);
    await cas.assertVisibility(page2, "#token");
    await cas.type(page2, "#token", code);
    await cas.submitForm(page2, "#fm1");
    await cas.sleep(2000);
    await cas.assertTextContentStartsWith(page2, "div .banner-danger p", "Multifactor authentication attempt has failed");
    await page2.close();
    
    await page.bringToFront();
    await cas.attributeValue(page, "html", "lang", "en");
    await cas.type(page, "#token", "unknownCode");
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);
    await cas.assertTextContentStartsWith(page, "div .banner-danger p", "Multifactor authentication attempt has failed");

    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1", undefined, 401);
    await cas.sleep(1000);

    await cas.assertVisibility(page, "#resendButton");
    await cas.click(page, "#resendButton");
    await cas.sleep(1000);

    code = await cas.extractFromEmail(browser);
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1", undefined, 200);
    await cas.sleep(1000);
    
    await cas.submitForm(page, "#registerform");
    await cas.sleep(3000);

    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);

    await cas.closeBrowser(browser);
})();
