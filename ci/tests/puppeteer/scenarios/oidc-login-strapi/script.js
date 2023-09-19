const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");

    await cas.loginWith(page);
    await cas.assertCookie(page);

    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");

    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    // hit strapi endpoint that triggers CAS login to get JWT
    await cas.goto(page, "http://localhost:1337/api/connect/cas");
    await page.waitForTimeout(2000);
    let element = await page.$('body pre');
    if (element == null) {
        let errorpage = await cas.textContent(page, 'body div main');
        await cas.log(errorpage);
        throw "failed";
    }
    let jwt = await page.evaluate(element => element.textContent.trim(), element);
    await cas.log(jwt);
    assert(jwt.includes("jwt"));
    await browser.close();
})();
