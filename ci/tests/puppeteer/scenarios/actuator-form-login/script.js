const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/actuator/info");

    await cas.assertInnerText(page, "#content h1", "Login");
    await cas.assertVisibility(page, '#content form[name=fm1]');

    await cas.assertInnerText(page, "#content form[name=fm1] h3", "Enter Username & Password");
    await cas.assertVisibility(page, '#username');

    let uid = await page.$('#username');
    assert("none" === await uid.evaluate(el => el.getAttribute("autocapitalize")));
    assert("false" === await uid.evaluate(el => el.getAttribute("spellcheck")));
    assert("username" === await uid.evaluate(el => el.getAttribute("autocomplete")));
    await cas.assertVisibility(page, '#password');

    let response = await cas.loginWith(page, "unknown", "badpassword");
    await page.waitForTimeout(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    await cas.screenshot(page);
    assert(response.status() === 200);
    await cas.assertVisibility(page, "#errorPanel");
    await cas.assertInnerText(page, "#errorPanel", "Invalid credentials.");

    response = await cas.loginWith(page);
    await cas.log(`${response.status()} ${response.statusText()}`);
    await cas.screenshot(page);
    const content = await page.content();
    let json = await cas.substring(content, "<pre>", "</pre>");
    let payload = JSON.parse(json);
    await cas.log(payload);

    await browser.close();
})();
