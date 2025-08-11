
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await submitLoginFailure(page);
    await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");
    await submitLoginFailure(page);
    await cas.assertInnerText(page, "#content h2", "Access Denied");
    await cas.assertInnerText(page, "#content p", "You've entered the wrong password for the user too many times. You've been throttled.");

    const url = "https://localhost:8443/cas/actuator/throttles";
    await cas.log(`Trying ${url}`);
    const response = await cas.goto(page, url);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await cas.closeBrowser(browser);
})();

async function submitLoginFailure(page) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, "casuser", "BadPassword1");
}

