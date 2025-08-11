
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "mustchangepswd", "password");
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#pwdmain h3", "Hello, mustchangepswd. You must change your password.");
    await cas.type(page,"#currentPassword", "password");
    await cas.type(page,"#password", "Jv!e0mKD&dCNl^Q");
    await cas.type(page,"#confirmedPassword", "Jv!e0mKD&dCNl^Q");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#content h2", "Password Change Successful");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "mustchangepswd", "Jv!e0mKD&dCNl^Q");
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.closeBrowser(browser);
})();
