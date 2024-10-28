
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const service1 = "https://localhost:9859/anything/sample";
    await login(page, service1, "mfa-duo");
    await cas.sleep(2000);

    const service2 = "https://localhost:9859/anything/open";
    await login(page, service2, "mfa-duo-alt");
    await cas.sleep(2000);

    await cas.gotoLogin(page, service1);
    await cas.sleep(2000);
    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service1}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    const json = JSON.parse(body);
    const success = json.serviceResponse.authenticationSuccess;
    assert(success.attributes.duoSub[0] !== undefined);
    assert(success.attributes.authenticationMethod[0] === "AlternativeDuoSecurity");
    assert(success.attributes.username[0] === "duobypass");
    assert(success.attributes.duoAuthCtxTxId[0] !== undefined);
    assert(success.attributes.duoAuthResultStatus[0] !== undefined);
    assert(success.attributes.authnContextClass[0] === "mfa-duo");

    await browser.close();
})();

async function login(page, service, providerId) {
    await cas.gotoLogout(page);
    await cas.sleep(1000);
    await cas.assertCookie(page, false);
    await cas.log(`Trying with service ${service}`);
    await cas.gotoLogin(page, service);
    await cas.sleep(2000);
    await cas.loginWith(page, "duobypass", "Mellon");
    await cas.sleep(4000);
    await cas.screenshot(page);
    await cas.logPage(page);
    const ticket = await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.screenshot(page);
    await page.waitForSelector("#content", {visible: true});
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", providerId);
    await cas.screenshot(page);
    return ticket;
}
