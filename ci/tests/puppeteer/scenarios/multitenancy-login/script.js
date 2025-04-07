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

        response = await cas.loginWith(page, "casuser", "Mellon");
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

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    await verifyTenant(browser, "shire");
    await cas.separator();
    await verifyTenant(browser, "moria");
    await browser.close();
})();
