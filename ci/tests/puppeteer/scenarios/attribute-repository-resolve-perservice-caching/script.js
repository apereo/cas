const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');
const fs = require("fs");
const path = require("path");

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
    let ticket = await cas.assertTicketParameter(page);
    let body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    console.log(body)
    let json = JSON.parse(body).serviceResponse.authenticationSuccess.attributes;
    assert(json.lastName[0] === "Johnson");
    assert(json.employeeNumber[0] === "123456");
    let originalFirstName = json.firstName[0];
    assert(originalFirstName !== null);
    assert(json.displayName === undefined)

    let newFirstName = (Math.random() + 1).toString(36).substring(4);
    await cas.logg(`Generated new first name ${newFirstName}`)
    let configFilePath = path.join(__dirname, '/attribute-repository.json')
    let config = JSON.parse(fs.readFileSync(configFilePath));
    config.casuser.firstName[0] = newFirstName;
    await cas.logg(`Writing configuration ${JSON.stringify(config)}`)
    await fs.writeFileSync(configFilePath, JSON.stringify(config));
    await cas.sleep(1000)

    for (let i = 1; i <= 3; i++) {
        await cas.logg(`Validation attempt ${i}; still within cache time window`)
        body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
        console.log(body)
        json = JSON.parse(body).serviceResponse.authenticationSuccess.attributes;
        assert(json.firstName[0] === originalFirstName);
    }
    await cas.sleep(5500)
    await cas.logg(`Validating again to get new attribute updates, expecting ${newFirstName}`)
    body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    console.log(body)
    json = JSON.parse(body).serviceResponse.authenticationSuccess.attributes;
    assert(json.firstName[0] === newFirstName);

    await browser.close();
})();
