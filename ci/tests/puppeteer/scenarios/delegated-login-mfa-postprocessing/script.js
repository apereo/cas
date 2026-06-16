
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const service1 = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service1);
    await cas.sleep(1000);

    const loginProviders = await page.$("#loginProviders");
    assert(loginProviders !== null);
    await cas.click(page, "li #CASServerOne");
    await cas.waitForNavigation(page);

    await cas.sleep(3000);
    await cas.screenshot(page);
    await cas.loginWith(page);
    await cas.sleep(3000);

    const result = new URL(page.url());
    await cas.log(result.searchParams.toString());
    let ticket = await cas.assertTicketParameter(page);
    let json = await cas.validateTicket(service1, ticket);
    let authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes["authnContextClass"] !== undefined);

    const service2 = "https://localhost:9859/anything/sample";
    await cas.gotoLogin(page, service2);
    await cas.sleep(1000);
    ticket = await cas.assertTicketParameter(page);
    json = await cas.validateTicket(service2, ticket);
    authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes["authnContextClass"] !== undefined);

    await cas.gotoLogout(page);
    await cas.closeBrowser(browser);
})();

