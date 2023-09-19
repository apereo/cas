const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");

    await cas.loginWith(page, "casuser", "Mellon");
    await cas.screenshot(page);
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertTextContent(page, "#interruptMessage", "We interrupted your login");
    await cas.assertCookie(page);
    await cas.assertVisibility(page, '#interruptLinks');
    await page.waitForTimeout(3000);
    const url = `${page.url()}`;
    await cas.log(`Page URL: ${url}`);
    assert(url.includes("https://www.google.com"));
    await browser.close();
})();
