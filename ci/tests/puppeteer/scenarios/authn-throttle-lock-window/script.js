const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);

    await cas.log("Original log in attempt");
    await submitLoginFailure(page);
    await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");

    await cas.log("Log in attempt should be throttled");
    await submitLoginFailure(page);
    await cas.assertInnerTextContains(page, "#content p", "You've been throttled.");

    for (let i = 1; i <= 2; i++) {
        await cas.log(`Log in attempt should remain throttled: ${i}`);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await page.waitForTimeout(1000);
    }
    await page.waitForTimeout(3000);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);

    await browser.close();
})();

async function submitLoginFailure(page) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, "casuser", "BadPassword1");
}


