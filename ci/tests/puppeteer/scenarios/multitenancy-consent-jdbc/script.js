const cas = require("../../cas.js");

async function verifyTenant(browser, tenantId) {
    const context = await browser.createBrowserContext();

    const service = "https://localhost:9859/anything/cas";
    const page = await cas.newPage(browser);
    await cas.gotoLogoutForTenant(page, tenantId);
    await cas.gotoLoginForTenant(page, tenantId, service);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.assertTextContent(page, "#name", "name");
    await cas.assertTextContent(page, "#gender", "gender");
    await cas.assertTextContent(page, "#email", "email");

    await cas.click(page, "#confirm");
    await cas.waitForNavigation(page);
    await cas.sleep(2000);
    await cas.assertTicketParameter(page);

    await cas.gotoLogoutForTenant(page, tenantId);
    await context.close();
}

(async () => {
    for (const tenantId of ["shire", "moria"]) {
        await cas.log("Removing previous consent decisions for casuser");
        await cas.doDelete("https://localhost:8443/cas/actuator/attributeConsent/casuser", 0, undefined, undefined,
            {
                "X-Tenant-Id": tenantId
            });
    }

    const browser = await cas.newBrowser(cas.browserOptions());
    await verifyTenant(browser, "shire");
    await cas.separator();
    await verifyTenant(browser, "moria");
    
    await cas.closeBrowser(browser);
})();
