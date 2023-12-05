const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        const service = "https://apereo.github.io";
        await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
        await page.waitForTimeout(1000);
        await cas.loginWith(page);
        const ticket = await cas.assertTicketParameter(page);
        const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
        let json = JSON.parse(body);
        console.dir(json, {depth: null, colors: true});
        let success = json.serviceResponse.authenticationSuccess;
        assert(success.attributes.mail !== undefined);
        assert(success.attributes["external-groups"] !== undefined);
    } finally {
        await browser.close();
    }
})();


