const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogout(page);
    await cas.gotoLogin(page, "https://apereo.github.io");
    await page.waitForTimeout(1000);
    await cas.loginWith(page);
    await page.waitForTimeout(2000);
    await cas.assertParameter(page, "ticket");

    await cas.log("Trying delegated authentication to activate access strategy");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, "https://apereo.github.io");
    await page.waitForTimeout(1000);
    await cas.assertVisibility(page, 'li #CASClient');
    await cas.click(page, "#CASClient");
    await page.waitForNavigation();
    await page.waitForTimeout(1000);
    let response = await cas.loginWith(page);
    await cas.log(`${response.status()} ${response.statusText()}`);
    await page.waitForTimeout(1000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#content h2", "Unauthorized Access");
    await cas.assertTextContentStartsWith(page, "#content div p", "Either the authentication request was rejected/cancelled");
    assert(response.status() === 401);
    await browser.close();
})();


