
const assert = require("assert");
const path = require("path");
const cas = require("../../cas.js");

async function normalAuthenticationFlow(context) {
    const page = await cas.newPage(context);

    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.loginWith(page);
    await cas.sleep(3000);
    await page.waitForSelector("#table_with_attributes", {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");

    const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);
    await cas.sleep(1000);

    await cas.log("Removing cached metadata for service providers");
    await cas.doDelete("https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadataCache");
    const entityId = "http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp";
    const endpoints = ["health", `samlIdPRegisteredServiceMetadataCache?serviceId=Sample&entityId=${entityId}`];
    const baseUrl = "https://localhost:8443/cas/actuator/";
    for (let i = 0; i < endpoints.length; i++) {
        const url = baseUrl + endpoints[i];
        const response = await cas.goto(page, url);
        await cas.log(`${response.status()} ${response.statusText()}`);
        assert(response.ok());
    }
    const response = await cas.goto(page, "https://localhost:8443/cas/idp/error");
    assert(response.ok());
    await cas.assertInnerText(page, "#content h2", "SAML2 Identity Provider Error");

    await cas.gotoLogout(page);
}

async function staleAuthenticationFlow(context) {
    const page = await cas.newPage(context);
    
    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.sleep(2000);
    await cas.screenshot(page);

    await cas.log("Checking for page URL...");
    const url = await page.url();
    await cas.logPage(page);

    await cas.log(`Restarting the flow with ${url}`);
    const page2 = await cas.newPage(context);
    await cas.goto(page2, url);
    await cas.sleep(2000);
    await cas.loginWith(page2);
    await cas.sleep(3000);
    await page2.waitForSelector("#table_with_attributes", {visible: true});
    await cas.assertInnerTextContains(page2, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page2, "#table_with_attributes");
    const authData = JSON.parse(await cas.innerHTML(page2, "details pre"));
    await cas.log(authData);
    await cas.sleep(1000);
    await cas.goto(page2, "https://localhost:8443/cas/logout");
    await cas.log("Done");
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    for (let i = 1; i <= 2; i++) {
        const context = await browser.createBrowserContext();
        await cas.log(`Running test scenario ${i}`);
        switch (i) {
        case 1:
            await cas.log("Running test for normal authentication flow");
            await normalAuthenticationFlow(context);
            break;
        case 2:
            await cas.log("Running test for stale authentication flow");
            await staleAuthenticationFlow(context);
            break;
        }
        await context.close();
        await cas.log("=======================================");
    }

    const samlMetrics = [
        "resolve"
    ];

    const baseUrl = "https://localhost:8443/cas/actuator/";
    for (let i = 0; i < samlMetrics.length; i++) {
        const url = `${baseUrl}metrics/org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver.${samlMetrics[i]}`;
        await cas.log(`Trying ${url}`);
        await cas.doRequest(url, "GET", {
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200);
    }
    
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();

