const cas = require("../../cas.js");
const assert = require("assert");

async function verifyTenant(browser, tenantId) {
    const context = await browser.createBrowserContext();

    const page = await cas.newPage(browser);
    let response = await cas.gotoLogin(page);
    assert(response.status() === 404);
    response = await cas.gotoLogout(page);
    assert(response.status() === 404);

    await cas.gotoLoginForTenant(page, tenantId);
    await cas.sleep(1000);

    switch (tenantId) {
    case "shire":
        await cas.assertVisibility(page, "li #CasClient1");
        await cas.assertInvisibility(page, "li #CasClient2");
        await cas.assertInvisibility(page, "#casFooter");
        await cas.assertInnerText(page, "#securityNotice", "Close your browser");

        response = await cas.loginWith(page, "casweb", "p@ssw0rd");
        assert(response.status() === 401);
        await cas.loginWith(page, "casadmin", "P@ssw0rd");
        break;
    case "moria":
        await cas.assertVisibility(page, "li #CasClient2");
        await cas.assertVisibility(page, "#casFooter");
        await cas.assertInvisibility(page, "li #CasClient1");
        await cas.assertInnerTextStartsWith(page, "#securityNotice", "For security reasons");

        response = await cas.loginWith(page);
        assert(response.status() === 401);
        await cas.loginWith(page, "casweb", "p@ssw0rd");
        break;
    }
    await cas.sleep(1000);
    const cookie = await cas.assertCookie(page);
    assert(cookie.path === `/cas/tenants/${tenantId}`);

    const page2 = await cas.newPage(browser);
    await cas.gotoLoginForTenant(page2, tenantId);
    await cas.sleep(1000);
    await cas.assertCookie(page2);
    await cas.gotoLogoutForTenant(page2, tenantId);
    await cas.sleep(1000);
    await context.close();
}

async function verifySessionStaysInTenant(browser) {
    const service = "https://localhost:9859/anything/cas";
    const page = await cas.newPage(browser);
    await cas.gotoLoginForTenant(page, "shire");
    await cas.sleep(1000);
    await cas.loginWith(page, "casadmin", "P@ssw0rd");
    await cas.sleep(1000);
    const cookie = await cas.assertCookie(page);
    await page.setCookie({...cookie, path: "/cas/tenants/moria"});

    await cas.gotoLoginForTenant(page, "moria", service);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page, false);
    await cas.assertVisibility(page, "#username");

    await cas.doRequest("https://localhost:8443/cas/tenants/shire/login", "GET", {"X-Tenant-Id": "moria"}, 400);
    await cas.doRequest("https://localhost:8443/cas/tenants/shire/login", "GET", {"X-Tenant-Id": "shire"}, 200);
    await cas.gotoLogoutForTenant(page, "shire");
    await page.close();
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    await verifyTenant(browser, "shire");
    await cas.separator();
    await verifyTenant(browser, "moria");
    await cas.separator();
    await verifySessionStaysInTenant(browser);
    await cas.closeBrowser(browser);
})();
