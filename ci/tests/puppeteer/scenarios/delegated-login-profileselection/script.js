const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.assertVisibility(page, 'li #CasClient');
    await cas.click(page, "li #CasClient");
    await page.waitForNavigation();

    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    await cas.loginWith(page);
    await page.waitForTimeout(2000);
    await cas.assertInnerText(page, '#content div h2', "Delegated Authentication Profile Selection");
    await cas.assertPageTitleContains(page, "Delegated Authentication Profile Selection");

    let profileId = await cas.innerText(page, "#profilesTable tr td code");
    await cas.log(profileId);
    await cas.submitForm(page, `#profilesTable #form-${profileId}`);
    await page.waitForTimeout(2000);

    await cas.assertCookie(page);
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    let principalId = await cas.innerText(page, "span#principalId");
    assert(principalId === profileId);
    
    await browser.close();
})();
