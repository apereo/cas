const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    let failed = false;
    const browser = await cas.newBrowser(cas.browserOptions());
    try {

        const page = await cas.newPage(browser);
        await cas.updateDuoSecurityUserStatus("duocode");

        const service = "https://localhost:9859/anything/attributes";
        await cas.gotoLoginWithAuthnMethod(page, service, "mfa-duo");
        await cas.sleep(2000);

        await cas.loginWith(page, "duocode", "Mellon");
        await cas.sleep(4000);
        let bypassCodes = await cas.fetchDuoSecurityBypassCodes("duocode");
        await cas.loginDuoSecurityBypassCode(page, "duocode", bypassCodes);
        await cas.sleep(4000);
        await cas.screenshot(page);

        const ticket = await cas.assertTicketParameter(page);
        const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
        await cas.logg(body);
        const json = JSON.parse(body.toString());
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.user === "duocode");
        assert(authenticationSuccess.attributes.duoAuthCtxTxId[0] !== undefined);
        assert(authenticationSuccess.attributes.duoAuthCtxAccessDeviceIp[0] !== undefined);
        assert(authenticationSuccess.attributes.authnContextClass[0] === "mfa-duo");
        assert(authenticationSuccess.attributes.duoAuthCtxFactor[0] === "bypass_code");
        assert(authenticationSuccess.attributes.duoAuthCtxReason[0] === "valid_passcode");
        assert(authenticationSuccess.attributes.duoAuthResultStatus[0] === "allow");
        assert(authenticationSuccess.attributes.duoAuthResult[0] === "allow");
        assert(authenticationSuccess.attributes.duoAud[0] !== undefined);
        assert(authenticationSuccess.attributes.duoIat[0] !== undefined);
        assert(authenticationSuccess.attributes.duoPreferredUsername[0] === "duocode");
        assert(authenticationSuccess.attributes.userAgent[0] !== undefined);
        assert(authenticationSuccess.attributes.duoSub[0] === "duocode");
        assert(authenticationSuccess.attributes.username[0] === "duocode");
        assert(authenticationSuccess.attributes["first-name"][0] === "Apereo");
        assert(authenticationSuccess.attributes["last-name"][0] === "CAS");
        assert(authenticationSuccess.attributes["email"][0] === "casuser@example.org");

        await cas.gotoLogin(page);
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
        await cas.assertCookie(page);
        await cas.gotoLogout(page);

        await cas.log("Testing an application that requires a POST response type...");
        await cas.updateDuoSecurityUserStatus("duocode");
        await cas.gotoLogin(page, "https://localhost:9859/anything/postservice");
        await cas.loginWith(page, "duocode", "Mellon");
        await cas.sleep(7000);
        bypassCodes = await cas.fetchDuoSecurityBypassCodes("duocode");
        await cas.loginDuoSecurityBypassCode(page, "duocode", bypassCodes);
        await cas.sleep(6000);
        await cas.screenshot(page);
        await cas.logPage(page);
        let content = await cas.textContent(page, "body");
        let payload = JSON.parse(content);
        assert(payload.form.ticket !== undefined);
        assert(payload.method === "POST");
        await cas.gotoLogout(page);

        await cas.log("Testing an application that requires a POST response type as a parameter..");
        await cas.updateDuoSecurityUserStatus("duocode");
        await cas.gotoLogin(page, "https://localhost:9859/anything/postmethod", 8443, false, "POST");
        await cas.loginWith(page, "duocode", "Mellon");
        await cas.sleep(7000);
        bypassCodes = await cas.fetchDuoSecurityBypassCodes("duocode");
        await cas.loginDuoSecurityBypassCode(page, "duocode", bypassCodes);
        await cas.sleep(7000);
        await cas.screenshot(page);
        await cas.logPage(page);
        content = await cas.textContent(page, "body");
        payload = JSON.parse(content);
        assert(payload.form.ticket !== undefined);
        assert(payload.method === "POST");
        await cas.gotoLogout(page);
    } catch (e) {
        failed = true;
        throw e;
    } finally {
        await browser.close();
        if (!failed) {
            await process.exit(0);
        }
    }
})();
