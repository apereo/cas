const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    let failed = false;
    try {
        const page = await cas.newPage(browser);
        const service = "https://localhost:9859/anything/sample";
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
        await cas.closeBrowser(browser);

        const json = await cas.validateTicket(service, ticket);
        const attributes = json.serviceResponse.authenticationSuccess.attributes;
        assert(attributes.cn === "casuser");
        assert(attributes.mail === "casuser@example.org");
    } catch (e) {
        failed = true;
        throw e;
    } finally {
        await cas.closeBrowser(browser);
        if (!failed) {
            await process.exit(0);
        }
    }
})();
