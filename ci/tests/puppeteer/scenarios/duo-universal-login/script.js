const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');
const fs = require('fs');
const cas = require('../../cas.js');

(async () => {
    let args = process.argv.slice(2);
    let config = JSON.parse(fs.readFileSync(args[0]));
    assert(config != null)
    
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-duo");
    await cas.loginWith(page, "casuser", "Mellon");

    let result = new URL(page.url());
    assert(result.host === "api-d2e616a0.duosecurity.com");
    await browser.close();
})();
