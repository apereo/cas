const cas = require("../../cas.js");
const assert = require("assert");

async function loginAndRegisterTrustedDevice(browser) {
    const context = await browser.createBrowserContext();

    try {
        const page = await cas.newPage(context);
        await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
        await cas.loginWith(page);
        await cas.sleep(1000);
        await cas.log("Using scratch code to login...");

        const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
        await cas.type(page,"#token", scratch);
        
        await cas.submitForm(page, "#fm1");
        await cas.innerText(page, "#deviceName");
        await cas.type(page, "#deviceName", "My Trusted Device");
        await cas.sleep(1000);
        await cas.assertInvisibility(page, "#expiration");
        await cas.assertVisibility(page, "#timeUnit");
        await cas.submitForm(page, "#registerform");
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    } finally {
        await context.close();
    }
}

async function loginFromPublicWorkstation(browser) {
    const context = await browser.createBrowserContext();
    const service = "https://localhost:9859/anything/trusted";
    
    try {
        const page = await cas.newPage(context);
        await cas.gotoLoginWithAuthnMethod(page, service, "mfa-gauth");
        await cas.assertVisibility(page, "#publicWorkstationButton");
        await cas.click(page, "#publicWorkstationButton");
        await cas.sleep(1000);
        await cas.loginWith(page);
        await cas.sleep(1000);
        await cas.log("Using scratch code to login...");
        const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
        await cas.type(page,"#token", scratch);
        await cas.submitForm(page, "#fm1");
        await cas.sleep(1000);
        const ticket = await cas.assertTicketParameter(page);
        await cas.gotoLogin(page);
        await cas.assertCookie(page, false);
        
        const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
        await cas.log(body);
        const json = JSON.parse(body);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.user.includes("casuser"));

    } finally {
        await context.close();
    }
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    await loginAndRegisterTrustedDevice(browser);
    await loginFromPublicWorkstation(browser);
    await browser.close();
})();
