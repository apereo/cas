const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://apereo.github.io";
    await page.goto(`https://localhost:8443/cas/login?service=${service}`);
    await cas.loginWith(page, "duobypass", "Mellon");
    await cas.assertVisibility(page, '#twitter-link')
    await cas.assertVisibility(page, '#youtube-link')
    console.log(await page.url())
    await page.waitForTimeout(8000)
    await cas.assertTicketParameter(page);

    await page.goto(`https://localhost:8443/cas/login`);
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await cas.assertTicketGrantingCookie(page);

    const endpoints = ["duoPing", "duoAccountStatus/casuser", "duoAdmin/casuser?providerId=mfa-duo"];
    const baseUrl = "https://localhost:8443/cas/actuator/"
    for (let i = 0; i < endpoints.length; i++) {
        let url = baseUrl + endpoints[i];
        console.log(`Calling endpoint ${url}`)
        const response = await page.goto(url);
        console.log(`${response.status()} ${response.statusText()}`)
        assert(response.ok())
    }
    await browser.close();
})();
