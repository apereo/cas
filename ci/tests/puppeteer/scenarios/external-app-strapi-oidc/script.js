const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await cas.loginWith(page, "casuser", "Mellon");

    await cas.assertTicketGrantingCookie(page);
    
    const title = await page.title();
    console.log(title)
    assert(title === "CAS - Central Authentication Service")

    const header = await cas.innerText(page, '#content div h2');
    console.log(header)
    assert(header === "Log In Successful")
    // hit strapi endpoint that triggers CAS login to get JWT
    await page.goto("http://localhost:1337/connect/cas", {waitUntil: 'networkidle2'});
    let element = await page.$('body pre');
    if (element == null) {
        let element = await page.$('body div main');
        let errorpage = await page.evaluate(element => element.textContent.trim(), element);
        console.log(errorpage)
        assert(false);
    }
    let jwt = await page.evaluate(element => element.textContent.trim(), element);
    console.log(jwt);
    assert(jwt.includes("jwt"));
    await browser.close();
})();
