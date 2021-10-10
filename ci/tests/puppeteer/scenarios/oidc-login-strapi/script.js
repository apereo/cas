const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertTicketGrantingCookie(page);

    await cas.assertPageTitle(page, "CAS - Central Authentication Service");

    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    // hit strapi endpoint that triggers CAS login to get JWT
    await page.goto("http://localhost:1337/connect/cas", {waitUntil: 'networkidle2'});
    let element = await page.$('body pre');
    if (element == null) {
        let errorpage = await cas.textContent(page, 'body div main');
        console.log(errorpage)
        throw "failed";
    }
    let jwt = await page.evaluate(element => element.textContent.trim(), element);
    console.log(jwt);
    assert(jwt.includes("jwt"));
    await browser.close();
})();
