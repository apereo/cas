
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://github.com");

    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.gotoLogin(page);
    await cas.sleep(1000);

    await cas.assertCookie(page);

    await cas.gotoLogout(page);

    await cas.assertInnerText(page, "#content h2", "Do you, casuser, want to log out completely?");
    await cas.assertVisibility(page, "#logoutButton");
    await cas.assertVisibility(page, "#divServices");
    await cas.assertVisibility(page, "#servicesTable");
    await cas.submitForm(page, "#fm1");

    await cas.logPage(page);
    await cas.assertPageUrl(page, "https://localhost:8443/cas/logout");

    await cas.sleep(1000);
    await cas.assertCookie(page, false);

    await cas.log("Logout with redirect...");
    await cas.goto(page, "https://localhost:8443/cas/logout?url=https://github.com/apereo/cas");
    await cas.submitForm(page, "#fm1");
    await cas.logPage(page);
    await cas.assertPageUrl(page, "https://github.com/apereo/cas");

    await cas.log("Logout with unauthorized redirect...");
    const response = await cas.goto(page, "https://localhost:8443/cas/logout?url=https://google.com");
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() === 403);
    
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await cas.logPage(page);

    await cas.closeBrowser(browser);
})();
