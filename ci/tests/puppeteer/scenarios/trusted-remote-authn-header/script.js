const cas = require('../../cas.js');
const puppeteer = require("puppeteer");
const assert = require("assert");

(async () => {
    const casLoginUrl = "https://localhost:4443/cas/login";
    await cas.doRequest(casLoginUrl, "GET", {}, 401);
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.authenticate({'username':'admin', 'password': 'password'});
    let response = await cas.goto(page, casLoginUrl);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    await cas.assertCookie(page);
    await browser.close();
})();
