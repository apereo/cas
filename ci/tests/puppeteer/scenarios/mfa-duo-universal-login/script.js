const cas = require("../../cas.js");
const assert = require("assert");

async function verifyDuoEndpoints(duoUser) {
    const json = {"Accept": "application/json"};
    const actuator = "https://localhost:8443/cas/actuator";
    const ping = JSON.parse(await cas.doRequest(`${actuator}/duoPing`, "GET", json, 200));
    assert(ping["mfa-duo"].availability === true);
    assert(ping["mfa-duo"].duoApiHost === process.env.DUO_SECURITY_API_HOST);
    assert(Object.keys(JSON.parse(await cas.doRequest(`${actuator}/duoPing?providerId=unknown`, "GET", json, 200))).length === 0);

    const status = JSON.parse(await cas.doRequest(`${actuator}/duoAccountStatus/${duoUser}?providerId=mfa-duo`, "GET", json, 200));
    assert(status["mfa-duo"].accountStatus.username === duoUser);
    assert(status["mfa-duo"].accountStatus.status !== undefined);

    const admin = JSON.parse(await cas.doRequest(`${actuator}/duoAdmin/${duoUser}`, "GET", json, 200));
    assert(admin["mfa-duo"].username === duoUser);
    assert(admin["mfa-duo"].userId !== undefined && admin["mfa-duo"].userId.length > 0);
}

(async () => {
    let failed = false;
    const browser = await cas.newBrowser(cas.browserOptions());
    const context = await browser.createBrowserContext();
    const duoUser = "duocode";
    try {
        const page = await cas.newPage(context);
        await cas.updateDuoSecurityUserStatus(duoUser);
        await verifyDuoEndpoints(duoUser);

        const service = "https://localhost:9859/anything/attributes";
        await cas.gotoLoginWithAuthnMethod(page, service, "mfa-duo");
        await cas.sleep(3000);
        await cas.logPage(page);
        await cas.loginWith(page, duoUser, "Mellon");
        await cas.sleep(4000);
        const bypassCodes = await cas.fetchDuoSecurityBypassCodes(duoUser);

        await cas.loginDuoSecurityBypassCode(page, duoUser, bypassCodes);
        
        await cas.sleep(4000);
        await cas.screenshot(page);

        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(service, ticket);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.user === duoUser);
        assert(authenticationSuccess.attributes.duoAuthCtxTxId[0] !== undefined);
        assert(authenticationSuccess.attributes.duoAuthCtxAccessDeviceIp[0] !== undefined);
        assert(authenticationSuccess.attributes.authnContextClass[0] === "mfa-duo");
        assert(authenticationSuccess.attributes.duoAuthCtxFactor[0] === "bypass_code");
        assert(authenticationSuccess.attributes.duoAuthCtxReason[0] === "valid_passcode");
        assert(authenticationSuccess.attributes.duoAuthResultStatus[0] === "allow");
        assert(authenticationSuccess.attributes.duoAuthResult[0] === "allow");
        assert(authenticationSuccess.attributes.duoAud[0] !== undefined);
        assert(authenticationSuccess.attributes.duoIat[0] !== undefined);
        assert(authenticationSuccess.attributes.duoPreferredUsername[0] === duoUser);
        assert(authenticationSuccess.attributes.userAgent[0] !== undefined);
        assert(authenticationSuccess.attributes.duoSub[0] === duoUser);
        assert(authenticationSuccess.attributes.username[0] === duoUser);
        assert(authenticationSuccess.attributes["first-name"][0] === "Apereo");
        assert(authenticationSuccess.attributes["last-name"][0] === "CAS");
        assert(authenticationSuccess.attributes["email"][0] === "casuser@example.org");

        await cas.gotoLogin(page);
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
        await cas.assertCookie(page);
        await cas.gotoLogout(page);

        await cas.log("Testing an application that requires a POST response type...");
        await cas.gotoLogin(page, "https://localhost:9859/anything/postservice");
        await cas.loginWith(page, duoUser, "Mellon");
        await cas.sleep(7000);
        await cas.loginDuoSecurityBypassCode(page, duoUser, bypassCodes);
        await cas.sleep(6000);
        await cas.screenshot(page);
        await cas.logPage(page);
        let content = await cas.textContent(page, "body");
        let payload = JSON.parse(content);
        assert(payload.form.ticket !== undefined);
        assert(payload.method === "POST");
        await cas.gotoLogout(page);

        await cas.log("Testing an application that requires a POST response type as a parameter..");
        await cas.gotoLogin(page, "https://localhost:9859/anything/postmethod", 8443, false, "POST");
        await cas.loginWith(page, duoUser, "Mellon");
        await cas.sleep(5000);
        await cas.loginDuoSecurityBypassCode(page, duoUser, bypassCodes);
        await cas.sleep(5000);
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
        await context.close();
        await cas.closeBrowser(browser);
        if (!failed) {
            await process.exit(0);
        }
    }
})();
