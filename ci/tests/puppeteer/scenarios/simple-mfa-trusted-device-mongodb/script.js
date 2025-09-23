const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    await cas.refreshContext();
    await cas.sleep(4000);

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-simple", "de");
    await cas.loginWith(page);
    await cas.sleep(8000);
    await cas.assertVisibility(page, "#token");

    let code = await cas.extractFromEmail(browser);
    assert(code.includes("Token->"));
    code = code.substring(code.lastIndexOf(">") + 1);
    await cas.log(`Code to use is extracted as ${code}`);

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);
    await cas.submitForm(page, "#registerform");
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content div h2", "Anmeldung erfolgreich");
    await cas.assertCookie(page);

    const baseUrl = "https://localhost:8443/cas/actuator/multifactorTrustedDevices";
    const response = await cas.doRequest(baseUrl);
    const record = JSON.parse(response)[0];
    console.dir(record, {depth: null, colors: true});
    assert(record.id > 0);
    assert(record.name !== undefined);
    await cas.closeBrowser(browser);
})();
