const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://example.com";
    await page.goto("https://localhost:8443/cas/login?service=" + service);
    let uid = await page.$('#username');
    await cas.assertAttribute(uid, "autocapitalize", "none");
    await cas.assertAttribute(uid, "spellcheck", "false");
    await cas.assertAttribute(uid, "autocomplete", "username");
    
    await cas.loginWith(page, "casuser", "Mellon");
    let ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest('https://localhost:8443/cas/validate?service=' + service + "&ticket=" + ticket);
    console.log(body)
    assert(body === "yes\ncasuser\n")
    await browser.close();
})();
