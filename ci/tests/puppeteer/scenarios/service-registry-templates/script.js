const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    await cas.loginWith(page, "casuser", "Mellon");
    const url = await page.url();
    await cas.log(`Page url: ${url}`);
    await cas.assertTicketParameter(page);

    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices";
    await cas.doGet(baseUrl, res => {
        assert(res.status === 200);
        cas.log(`Services found: ${res.data[1].length}`);

        res.data[1].forEach(svc => {
            cas.log(`Checking service ${svc.name}-${svc.id}`);
            assert(svc.description === "My Application");
            assert(svc.attributeReleasePolicy.allowedAttributes[1].includes("email"));
            assert(svc.attributeReleasePolicy.allowedAttributes[1].includes("username"))
        })
        
    }, err => {
        throw err;
    }, {
        'Content-Type': 'application/json'
    });
    
    await browser.close();
})();
