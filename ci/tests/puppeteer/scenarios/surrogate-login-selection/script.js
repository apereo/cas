const puppeteer = require('puppeteer');
const assert = require('assert');

const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://example.org");

    await cas.loginWith(page, "+casuser", "Mellon");
    await cas.assertTextContent(page, "#titlePanel h2", "Choose Account")
    await cas.assertTextContentStartsWith(page, "#surrogateInfo", "You are provided with a list of accounts");
    await cas.assertVisibility(page, '#surrogateTarget')

    await cas.assertVisibility(page, '#submit')

    let cancel = await page.$('#cancel');
    assert(cancel == null);

    await cas.assertVisibility(page, '#login')

    await browser.close();
})();
