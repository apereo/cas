const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");

    let uid = await page.$('#username');
    assert("none" === await uid.evaluate(el => el.getAttribute("autocapitalize")));
    assert("false" === await uid.evaluate(el => el.getAttribute("spellcheck")));
    assert("username" === await uid.evaluate(el => el.getAttribute("autocomplete")));
    await cas.loginWith(page);
    
    const url = await page.url();
    await cas.log(`Page url: ${url}`);
    assert(url === "https://github.com/");
    await browser.close();
})();
