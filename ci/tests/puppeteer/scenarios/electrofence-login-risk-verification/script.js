
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/adaptive";
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#loginErrorsPanel p", "authentication attempt is determined to be risky");
    await cas.assertCookie(page, false);
    const body = await cas.extractFromEmail(browser);
    const link = body.substring(body.indexOf("link=") + 5);
    await cas.logg(`Verification link is ${link}`);
    const response = await cas.goto(page, link);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    await cas.assertInnerText(page, "#content h2", "Risky Authentication attempt is confirmed.");

    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);

    await cas.closeBrowser(browser);
})();
