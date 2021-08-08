const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    let uid = await page.$('#username');
    await cas.assertAttribute(uid, "autocapitalize", "none");
    await cas.assertAttribute(uid, "spellcheck", "false");
    await cas.assertAttribute(uid, "autocomplete", "username");
    
    await cas.loginWith(page, "casuser", "Mellon");
    
    const url = await page.url()
    console.log(`Page url: ${url}`)
    assert(url === "https://github.com/")
    await browser.close();
})();
