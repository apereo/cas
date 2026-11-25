
const cas = require("../../cas.js");
const assert = require("assert");

const PHONE = "13477464523";

(async () => {
    const sms = await cas.mockSmsServer();
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.loginWith(page, "unknown+casuser", "Mellon");
    await cas.assertInnerTextStartsWith(page, "#loginErrorsPanel p", "You are not authorized to impersonate");
    const refused = await sms.waitForMessage(PHONE, (msg) => msg.text.includes("CasSurrogateAuthenticationFailureEvent"));
    assert(refused.from === "3477465300");
    assert(refused.text.startsWith("Impersonation:"), `Unexpected SMS text ${refused.text}`);
    assert(refused.text.includes("surrogate=unknown"), `Unexpected SMS text ${refused.text}`);

    await cas.gotoLogin(page);
    await cas.loginWith(page, "user3+casuser", "Mellon");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged into the Central Authentication Service");
    const allowed = await sms.waitForMessage(PHONE, (msg) => msg.text.includes("CasSurrogateAuthenticationSuccessfulEvent"));
    assert(allowed.text.startsWith("Impersonation:"), `Unexpected SMS text ${allowed.text}`);
    assert(allowed.text.includes("surrogate=user3"), `Unexpected SMS text ${allowed.text}`);

    const page2 = await browser.newPage();
    await page2.goto("http://localhost:8282");
    await cas.sleep(1000);
    await cas.click(page2, "table tbody td a");
    await cas.sleep(1000);

    await cas.closeBrowser(browser);
    await sms.close();
})();
