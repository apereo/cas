
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.log("Using scratch code to login...");
    let scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.type(page,"#token", scratch);
    await cas.submitForm(page, "#fm1");

    await cas.innerText(page, "#deviceName");
    await cas.type(page, "#deviceName", "My Trusted Device");
    await cas.sleep(1000);

    await cas.assertInvisibility(page, "#expiration");
    await cas.assertVisibility(page, "#timeUnit");
    await cas.submitForm(page, "#registerform");
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.sleep(1000);
    await cas.gotoLogout(page);
    await cas.sleep(2000);

    const baseUrl = "https://localhost:8443/cas/actuator/multifactorTrustedDevices";
    const response = await cas.doRequest(baseUrl);
    const record = JSON.parse(response)[0];
    console.dir(record, {depth: null, colors: true});
    assert(record.id !== undefined);
    assert(record.name !== undefined);

    const postgres = await cas.dockerContainer("postgres-server");
    await cas.log("Pausing postgres server...");
    await postgres.pause();

    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
    await cas.loginWith(page);
    await cas.sleep(3000);
    await cas.log("Using scratch code to login...");
    scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    await cas.type(page,"#token", scratch);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(3000);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);
    await cas.log("Resuming postgres server...");
    await postgres.unpause();
    await cas.closeBrowser(browser);
})();
