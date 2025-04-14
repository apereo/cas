
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#forgotPasswordLink", "Reset your password");
    await cas.click(page, "#forgotPasswordLink");
    await cas.sleep(2000);

    await cas.type(page,"#username", "duobypass");
    await cas.pressEnter(page);
    await cas.sleep(3000);
    await cas.assertInnerText(page, "#content h2", "Password Reset Instructions Sent Successfully.");
    await cas.assertInnerTextStartsWith(page, "#content p", "You should shortly receive a message");

    const link = await cas.extractFromEmail(browser);
    assert(link !== undefined);
    await cas.goto(page, link);
    await cas.sleep(10000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#pwdmain h3", "Hello, duobypass. You must change your password.");
    await cas.type(page,"#password", "Jv!e0mKD&dCNl^Q");
    await cas.type(page,"#confirmedPassword", "Jv!e0mKD&dCNl^Q");
    await cas.pressEnter(page);
    await cas.sleep(2000);
    await cas.assertInnerText(page, "#content h2", "Password Change Successful");
    
    await browser.close();
})();
