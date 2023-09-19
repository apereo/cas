const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://example.com";
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    let uid = await page.$('#username');
    assert("none" === await uid.evaluate(el => el.getAttribute("autocapitalize")));
    assert("false" === await uid.evaluate(el => el.getAttribute("spellcheck")));
    assert("username" === await uid.evaluate(el => el.getAttribute("autocomplete")));
    await cas.loginWith(page);
    let ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/validate?service=${service}&ticket=${ticket}`);
    await cas.log(body);
    assert(body === "yes\ncasuser\n");
    await browser.close();
})();
