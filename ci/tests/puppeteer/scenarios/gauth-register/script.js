const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-gauth");
    await cas.loginWith(page, "casuser", "Mellon");

    await cas.assertInnerTextStartsWith(page, "#login h4", "Your account is not registered");
    await cas.assertVisibility(page, '#login table img')
    await cas.assertVisibility(page, '#seckeypanel pre')
    await cas.assertVisibility(page, '#scratchcodes')
    assert(5 === (await page.$$('#scratchcodes div.mdc-chip')).length)

    let confirm = await page.$("#confirm");
    await confirm.click();
    await cas.assertVisibility(page, '#confirm-reg-dialog #notif-dialog-title')
    await cas.assertVisibility(page, '#token')
    await cas.assertVisibility(page, '#accountName')
    await browser.close();
})();
