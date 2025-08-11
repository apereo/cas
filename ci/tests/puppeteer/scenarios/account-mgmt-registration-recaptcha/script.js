
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.sleep(2000);
    await cas.assertTextContent(page, "#accountSignUpLink", "Sign Up");
    await cas.submitForm(page, "#accountMgmtSignupForm");
    await cas.sleep(1000);

    await cas.assertInnerText(page, "#content h2", "Account Registration");

    await cas.type(page,"#username", "casuser");
    await cas.type(page,"#firstName", "CAS");
    await cas.type(page,"#lastName", "Person");
    await cas.type(page,"#email", "cas@example.org");
    await cas.type(page,"#phone", "+1 347 745 4321");
    await cas.click(page, "#submit");
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.assertTextContent(page, "div .banner-danger p", "reCAPTCHA’s validation failed.");
    await cas.closeBrowser(browser);
})();
