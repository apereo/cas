const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.sleep(2000);
    await cas.assertInnerText(page, "#forgotPasswordLink", "Reset your password");
    
    await cas.click(page, "#forgotPasswordLink");
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#reset #fm1 h3", "Reset your password");
    await cas.assertVisibility(page, "#username");
    await cas.attributeValue(page, "#username", "autocapitalize", "none");
    await cas.attributeValue(page, "#username", "spellcheck", "false");
    await cas.attributeValue(page, "#username", "autocomplete", "username");

    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.sleep(1000);

    await cas.assertInnerText(page, "#content h2", "Password Reset Instructions Sent Successfully.");
    await cas.assertInnerTextStartsWith(page, "#content p", "You should shortly receive a message");

    await cas.goto(page, "http://localhost:8282");
    await cas.sleep(1000);
    await cas.click(page, "table tbody td a");
    await cas.sleep(1000);

    const link = await cas.textContent(page, "div[name=bodyPlainText] .well");
    await cas.goto(page, link);
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content #pwdmain h3", "Hello, casuser. You must change your password.");

    await cas.sleep(2000);
    await cas.goto(page, link);
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content #pwdmain h3", "Hello, casuser. You must change your password.");

    await cas.sleep(2000);
    await cas.goto(page, link);
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#main-content h2", "Password Reset Failed");

    await cas.sleep(2000);
    await browser.close();
})();
