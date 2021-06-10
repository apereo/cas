const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/actuator/sso");

    let header = await cas.innerText(page, '#content h2');

    assert(header === "Login")

    await cas.assertVisibility(page, '#content form[name=fm1]')

    let subtitle = await cas.innerText(page, '#content form[name=fm1] h3');
    assert(subtitle === "Enter Username & Password");

    await cas.assertVisibility(page, '#username')

    let uid = await page.$('#username');
    assert("none" === await uid.evaluate(el => el.getAttribute("autocapitalize")))
    assert("false" === await uid.evaluate(el => el.getAttribute("spellcheck")))
    assert("username" === await uid.evaluate(el => el.getAttribute("autocomplete")))

    await cas.assertVisibility(page, '#password')

    await browser.close();
})();
