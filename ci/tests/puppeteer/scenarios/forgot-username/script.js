const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    let link = await cas.textContent(page, "#forgotUsernameLink");
    assert(link === "Forgot your username?")

    await cas.click(page, "#forgotUsernameLink")

    // await page.click('#forgotUsernameLink');
    // await page.waitForNavigation();

    await page.waitForTimeout(1000)

    let header = await cas.textContent(page, "#reset #fm1 h3");
    assert(header === "Forgot your username?")

    await cas.assertVisibility(page, '#email')

    await cas.type(page,'#email', "casuser@example.org");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await cas.assertInnerTextStartsWith(page, "#content h2", "Instructions Sent Successfully.");
    await cas.assertInnerTextStartsWith(page, "#content p", "You should shortly receive a message");

    await browser.close();
})();

