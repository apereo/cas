
const path = require("path");
const cas = require("../../cas.js");
const assert = require("assert");

async function normalAuthenticationFlow(context) {
    const page = await cas.newPage(context);
    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.loginWith(page);
    await cas.sleep(6000);
    await page.waitForSelector("#table_with_attributes", {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");
    const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);

    assert(authData["Attributes"]["username"][0] === "casuser");
    assert(authData["Attributes"]["urn:oid:2.5.4.4"][0] === "CAS");
    assert(authData["Attributes"]["urn:oid:1.1.1.1.4.1.6666.1.1.1.10"].join(" ") === "Hi casuser");
    assert(authData["Attributes"]["urn:oid:0.9.2342.19200300.100.1.3"][0] === "casuser@example.org");

    assert(authData["Attributes"]["group"].includes("sys-admin"));
    assert(authData["Attributes"]["group"].includes("sys-control"));
    assert(authData["Attributes"]["group"].includes("sys-super"));

    await cas.sleep(2000);
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
    await cas.sleep(2000);
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
    await cas.sleep(2000);
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
        await cas.separator();
    }

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();

