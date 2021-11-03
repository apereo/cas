const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    await cas.doGet("https://localhost:8443/cas/actuator/resolveAttributes/casuser",
        function (res) {
            assert(res.data.uid !== null);
            assert(res.data.attributes !== null);
            assert(Object.keys(res.data.attributes).length === 0)
        }, function (error) {
            throw error;
        }, { 'Content-Type': "application/json" })

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://apereo.github.io";
    await page.goto(`https://localhost:8443/cas/login?service=${service}`);

    await cas.loginWith(page, "casuser", "Mellon");
    const url = await page.url()
    console.log(`Page url: ${url}`)
    let ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    console.log(body)
    let json = JSON.parse(body).serviceResponse.authenticationSuccess.attributes;
    assert(json.lastName[0] === "Johnson");
    assert(json.employeeNumber[0] === "123456");
    assert(json.firstName[0] === "Bob");
    
    assert(json.displayName === undefined)
    await browser.close();
})();
