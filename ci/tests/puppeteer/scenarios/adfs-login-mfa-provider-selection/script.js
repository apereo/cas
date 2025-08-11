
const cas = require("../../cas.js");

(async () => {
    const service = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.log(`Navigating to ${service}`);
    await cas.gotoLogin(page, service);
    await cas.sleep(2000);
    await cas.click(page, "div .idp span");
    await cas.sleep(4000);
    await cas.type(page, "#userNameInput", process.env.ADFS_USERNAME, true);
    await cas.type(page, "#passwordInput", process.env.ADFS_PASSWORD, true);
    await cas.sleep(1000);
    await cas.submitForm(page, "#loginForm");
    await cas.sleep(4000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#content h2", "Multifactor Authentication Provider Selection");
    await cas.assertVisibility(page, "#mfa-yubikey");
    await cas.assertVisibility(page, "#mfa-simple");
    await cas.closeBrowser(browser);
})();
