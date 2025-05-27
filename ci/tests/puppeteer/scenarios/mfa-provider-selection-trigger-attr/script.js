
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertVisibility(page, "#mfa-gauth");
    await cas.assertVisibility(page, "#mfa-webauthn");

    await cas.log("Selecting mfa-gauth");
    await cas.submitForm(page, "#mfa-gauth > form[name=fm-mfa-gauth]");
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#imageQRCode");
    await cas.assertVisibility(page, "#confirm");

    await cas.logb("Having selected a provider, future attempts should remember it");
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertVisibility(page, "#imageQRCode");
    await cas.assertVisibility(page, "#confirm");
    await browser.close();
})();
