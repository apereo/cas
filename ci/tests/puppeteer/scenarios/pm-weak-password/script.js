
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertCookie(page, false);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#content h2", "Weak Password Detected");
    await cas.click(page, "#submit");
    await cas.waitForNavigation(page);

    await cas.assertInnerText(page, "#pwdmain h3", "Hello, casuser. You must change your password.");
    await cas.type(page,"#currentPassword", "Mellon");
    await cas.type(page,"#password", "P@ssw0rd9");
    await cas.type(page,"#confirmedPassword", "P@ssw0rd9");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#content h2", "Password Change Successful");
    
    await cas.closeBrowser(browser);
})();
