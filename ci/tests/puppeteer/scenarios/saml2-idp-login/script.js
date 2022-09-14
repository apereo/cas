const puppeteer = require('puppeteer');
const assert = require('assert');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    try {
        await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
        await page.waitForTimeout(2000);
        await cas.screenshot(page);
        await cas.loginWith(page, "casuser", "Mellon");
        await page.waitForTimeout(3000);
        await page.waitForSelector('#table_with_attributes', {visible: true});
        await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
        await cas.assertVisibility(page, "#table_with_attributes");

        let authData = JSON.parse(await cas.innerHTML(page, "details pre"));
        console.log(authData);

        console.log("Removing cached metadata for service providers");
        await cas.doRequest("https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadataCache", "DELETE", {}, 204);

        const entityId = "http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp";
        const endpoints = ["health", `samlIdPRegisteredServiceMetadataCache?serviceId=Sample&entityId=${entityId}`];
        const baseUrl = "https://localhost:8443/cas/actuator/";
        for (let i = 0; i < endpoints.length; i++) {
            let url = baseUrl + endpoints[i];
            const response = await cas.goto(page, url);
            console.log(`${response.status()} ${response.statusText()}`);
            assert(response.ok())
        }
        const response = await cas.goto(page, "https://localhost:8443/cas/idp/error");
        assert(response.ok());
        await cas.assertInnerText(page, '#content h2', "SAML2 Identity Provider Error");

        await cas.goto(page, "https://localhost:8443/cas/logout");
        
    } finally {
        await cas.removeDirectory(path.join(__dirname, '/saml-md'));
        await browser.close();
    }
})();


