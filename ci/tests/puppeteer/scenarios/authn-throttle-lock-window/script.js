const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);

    console.log("Original log in attempt");
    await submitLoginFailure(page);
    await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");

    console.log("Log in attempt should be throttled");
    await submitLoginFailure(page);
    await cas.assertInnerTextContains(page, "#content p", "You've been throttled.");

    for (let i = 1; i <= 2; i++) {
        console.log(`Log in attempt should remain throttled: ${i}`);
        await cas.goto(page, "https://localhost:8443/cas/login");
        await cas.loginWith(page, "casuser", "Mellon");
        await page.waitForTimeout(1000);
    }
    await page.waitForTimeout(3000);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertCookie(page);

    await browser.close();
})();

async function submitLoginFailure(page) {
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "BadPassword1");
}


