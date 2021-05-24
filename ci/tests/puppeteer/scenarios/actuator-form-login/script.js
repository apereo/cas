const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/actuator/sso");

    let header = await page.$eval('#content h2', el => el.innerText)
    console.log(header)
    assert(header === "Login")

    let form = await page.$('#content form[name=fm1]');
    assert(await form.boundingBox() != null);

    let subtitle = await page.$eval('#content form[name=fm1] h3', el => el.innerText);
    console.log(subtitle);
    assert(subtitle === "Enter Username & Password");

    let uid = await page.$('#username');
    assert(await uid.boundingBox() != null);
    
    assert("none" === await uid.evaluate(el => el.getAttribute("autocapitalize")))
    assert("false" === await uid.evaluate(el => el.getAttribute("spellcheck")))
    assert("username" === await uid.evaluate(el => el.getAttribute("autocomplete")))

    let pswd = await page.$('#password');
    assert(await pswd.boundingBox() != null);

    await browser.close();
})();
