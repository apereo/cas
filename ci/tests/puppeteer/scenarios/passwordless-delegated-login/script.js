const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

async function startAuthFlow(page, username) {
    await cas.log("Removing previous sessions and logging out");
    await cas.gotoLogout(page);
    await cas.log(`Starting authentication flow for ${username}`);
    await cas.goto(page, "https://localhost:8443/cas/login?locale=en");
    await page.waitForTimeout(1000);
    let pswd = await page.$('#password');
    assert(pswd == null);
    await cas.type(page, '#username', username);
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await page.waitForTimeout(1000);
    const url = await page.url();
    await cas.logPage(page);
    assert(url.startsWith("https://localhost:8444"));
    await page.waitForTimeout(1000);

    await cas.loginWith(page);
    await page.waitForTimeout(5000);
    await cas.log(`Page url: ${await page.url()}`);
    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, casuser, have successfully logged in");

    await cas.click(page, "#auth-tab");
    await page.waitForTimeout(1000);
    await cas.type(page, "#attribute-tab-1 input[type=search]", "surrogate");
    await page.waitForTimeout(1000);
    await cas.screenshot(page);

    let surrogateEnabled = await page.$('#surrogateEnabled');
    assert(surrogateEnabled == null);
    let surrogatePrincipal = await page.$('#surrogatePrincipal');
    assert(surrogatePrincipal == null);
    let surrogateUser = await page.$('#surrogateUser');
    assert(surrogateUser == null);
    await page.waitForTimeout(1000);
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await startAuthFlow(page, "casuser-server");
    await startAuthFlow(page, "casuser-none");
    await startAuthFlow(page, "casuser-client");

    await browser.close();
})();
