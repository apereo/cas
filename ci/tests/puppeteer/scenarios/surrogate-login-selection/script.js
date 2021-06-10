const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://example.org");

    await cas.loginWith(page, "+casuser", "Mellon");
    
    // await page.waitForTimeout(20000)
    
    let header = await cas.textContent(page, "#titlePanel h2");
    assert(header === "Choose Account")

    header = await cas.textContent(page, "#surrogateInfo");

    assert(header.startsWith("You are provided with a list of accounts"));

    await cas.assertVisibility(page, '#surrogateTarget')

    await cas.assertVisibility(page, '#submit')

    let cancel = await page.$('#cancel');
    assert(cancel == null);

    await cas.assertVisibility(page, '#login')

    await browser.close();
})();
