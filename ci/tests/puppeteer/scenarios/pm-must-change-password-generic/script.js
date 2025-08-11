
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "mustchangepswd", "mustchangepswd");
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#pwdmain h3", "Hello, mustchangepswd. You must change your password.");
    await cas.type(page,"#currentPassword", "mustchangepswd");
    await cas.type(page,"#password", "Jv!e0mKD&dCNl^Q");
    await cas.type(page,"#confirmedPassword", "Jv!e0mKD&dCNl^Q");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#content h2", "Password Change Successful");
    await cas.closeBrowser(browser);
})();
