const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/actuator/sso");

    await cas.assertInnerText(page, "#content h2", "Login")
    await cas.assertVisibility(page, '#content form[name=fm1]')

    await cas.assertInnerText(page, "#content form[name=fm1] h3", "Enter Username & Password")
    await cas.assertVisibility(page, '#username')

    let uid = await page.$('#username');
    assert("none" === await uid.evaluate(el => el.getAttribute("autocapitalize")))
    assert("false" === await uid.evaluate(el => el.getAttribute("spellcheck")))
    assert("username" === await uid.evaluate(el => el.getAttribute("autocomplete")))
    await cas.assertVisibility(page, '#password')

    await browser.close();
})();
