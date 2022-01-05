const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://apereo.github.io&renew=true");
    await cas.assertVisibility(page, "#username")
    await cas.logg("Waiting for the service registry cache to expire...")
    await cas.sleep(3000)
    await page.goto("https://localhost:8443/cas/login?service=https://apereo.github.io&renew=true");
    await cas.assertVisibility(page, "#username")

    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices/type";
    await cas.doGet(`${baseUrl}/RegexRegisteredService`,
        res => {
            assert(res.status === 200)
            assert(res.data[1].length === 1)
        },
        error => {
            throw error;
        }, {'Content-Type': "application/json"});
    await cas.doGet(`${baseUrl}/OidcRegisteredService`,
        res => {
            assert(res.status === 200)
            assert(res.data[1].length === 0)
        },
        error => {
            throw error;
        }, {'Content-Type': "application/json"});

    await browser.close();
})();
