const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

async function startAuthFlow(page, username) {
    console.log(`Starting authentication flow for ${username}`);
    await cas.goto(page, "https://localhost:8443/cas/login?locale=en");
    let pswd = await page.$('#password');
    assert(pswd == null);
    await cas.type(page, '#username', username);
    await page.waitForTimeout(5000);
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(1000);
    const url = await page.url();
    console.log(`Page url: ${url}`);
    assert(url.startsWith("https://github.com/"));
    await page.waitForTimeout(5000);
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await startAuthFlow(page, "user3+casuser-server");
    await startAuthFlow(page, "user3+casuser-none");
    await startAuthFlow(page, "user3+casuser-client");

    await browser.close();
})();
