const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);

    await cas.logg("Log in attempt: #0 Successful Login");
    await submitLogin(page, "casuser", "Mellon");
    await cas.assertCookie(page);
    await cas.goto(page, "https://localhost:8443/cas/logout");
    await page.waitForTimeout(1000);

    await cas.log("Log in attempt: #1");
    await submitLogin(page);

    await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");

    await cas.log("Log in attempt: #2");
    await submitLogin(page);

    await cas.assertInnerText(page, "#content h2", "Access Denied");
    await cas.assertInnerText(page, "#content p", "You've entered the wrong password for the user too many times. You've been throttled.");

    // await cas.log("Closing page and trying again with bad credentials...")
    // await page.close();
    // page = await cas.newPage(browser);
    // await cas.log("Log in attempt: #2")
    // await submitLoginFailure(page);
    // await cas.assertInnerText(page, "#content p", "You've entered the wrong password for the user too many times. You've been throttled.")
    await browser.close();
})();

async function submitLogin(page, user = "casuser", password = "BadPassword1") {
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, user, password);
}


