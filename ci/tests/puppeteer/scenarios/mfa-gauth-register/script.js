
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth", "en");
    await cas.loginWith(page);

    await cas.assertInnerTextStartsWith(page, "#login h2", "Your account is not registered");
    await cas.assertVisibility(page, "img#imageQRCode");
    await cas.assertVisibility(page, "#seckeypanel pre");
    await cas.assertVisibility(page, "#scratchcodes");
    assert((await page.$$("#scratchcodes div.mdc-chip")).length === 5);

    const confirm = await page.$("#confirm");
    await confirm.click();
    await cas.assertVisibility(page, "#confirm-reg-dialog #notif-dialog-title");
    await cas.assertVisibility(page, "#token");
    await cas.assertVisibility(page, "#accountName");
    await browser.close();
})();
