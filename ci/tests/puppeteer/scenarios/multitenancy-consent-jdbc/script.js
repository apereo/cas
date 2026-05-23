const cas = require("../../cas.js");

async function verifyTenant(browser, tenantId) {
    const context = await browser.createBrowserContext();

    const service = "https://localhost:9859/anything/cas";
    const page = await cas.newPage(browser);
    await cas.gotoLoginForTenant(page, tenantId, service);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.assertTextContent(page, "#name", "name");
    await cas.assertTextContent(page, "#gender", "gender");
    await cas.assertTextContent(page, "#email", "email");

    await cas.click(page, "#confirm");
    await cas.waitForNavigation(page);
    await cas.assertTicketParameter(page);
    await context.close();
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    await verifyTenant(browser, "shire");
    await cas.separator();
    await verifyTenant(browser, "moria");
    await cas.closeBrowser(browser);
})();
