const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await doLogin(page, "syncopecas", "Mellon", "syncopecas@syncope.org")
    await doLogin(page, "casuser", "paSSw0rd", "casuser@syncope.org")
    await browser.close();
})();


async function doLogin(page, uid, psw, email) {
    await page.goto("https://localhost:8443/cas/logout");
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page,uid, psw);
    await cas.assertTicketGrantingCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await page.waitForTimeout(1000)
    const attributes = await cas.innerText(page, '#attribute-tab-0 table#attributesTable tbody');
    assert(attributes.includes("syncopeUserAttr_email"))
    assert(attributes.includes(email))
}
