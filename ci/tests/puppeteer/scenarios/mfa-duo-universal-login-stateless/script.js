
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://apereo.github.io";
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}&authn_method=mfa-duo`);
    await cas.updateDuoSecurityUserStatus("duocode");
    await cas.loginWith(page, "duocode", "Mellon");
    await cas.sleep(4000);
    await cas.loginDuoSecurityBypassCode(page, "duocode");
    await cas.sleep(4000);
    await cas.screenshot(page);
    await cas.logPage(page);
    const ticket = await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    const localStorageData = await cas.readLocalStorage(page);
    const browserStorage = JSON.parse(localStorageData["CAS"]);
    assert(browserStorage.CasBrowserStorageContext !== undefined);
    assert(browserStorage.DuoSecuritySessionContext !== undefined);
    await browser.close();

    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    const json = JSON.parse(body).serviceResponse.authenticationSuccess.attributes;
    assert(json.cn === "casuser");
    assert(json.mail === "casuser@example.org");
})();
