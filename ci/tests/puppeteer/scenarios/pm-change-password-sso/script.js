
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/login?doChangePassword=true&locale=en";
    await cas.goto(page, url);
    await cas.assertInnerText(page, "#content h2", "Password Reset Failed");
    
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.assertCookie(page);
    await cas.goto(page, url);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#pwdmain h3", "Hello, casuser. You must change your password.");
    await cas.attributeValue(page, ".generate-password", "title",  "Generate password");
    await cas.type(page,"#password", "Jv!e0mKD&dCNl^Q");
    await cas.type(page,"#confirmedPassword", "Jv!e0mKD&dCNl^Q");
    await cas.pressEnter(page);
    await cas.sleep(6000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#content h2", "Password Change Successful");
    await cas.closeBrowser(browser);
})();
