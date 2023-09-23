const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');
const path = require("path");
const fs = require("fs");
const os = require("os");

async function getActuatorEndpoint(entityId) {
    let baseEndpoint = "https://localhost:8443/cas/actuator/samlPostProfileResponse/logout/post";
    return `${baseEndpoint}?entityId=${entityId}`;
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await page.waitForTimeout(1000);
    await cas.loginWith(page);

    let ticket = await cas.assertTicketParameter(page);
    await cas.doRequest(`https://localhost:8443/cas/validate?service=${service}&ticket=${ticket}`);

    const endpoint = await getActuatorEndpoint("http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp");
    let sloPage = await cas.doPost(endpoint, {}, {
        'Content-Type': 'application/json'
    }, res => res.data, error => {
        throw(error);
    });
    const tempDir = os.tmpdir();
    let sloFile = `${tempDir}/saml2slo.html`;
    await fs.writeFileSync(sloFile, sloPage);
    await cas.log(`Logout page is written to ${sloFile}`);
    
    await cas.goto(page, `file://${sloFile}`);
    await page.waitForTimeout(4000);
    await cas.logPage(page);
    let url = await page.url();
    assert(url === "http://localhost:9443/simplesaml/module.php/saml/sp/saml2-logout.php/default-sp");
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();
