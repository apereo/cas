
const cas = require("../../cas.js");

(async () => {
    let browser = await cas.newBrowser(cas.browserOptions());
    let page = await cas.newPage(browser);

    await cas.log("Log in attempt: #1");
    await submitLoginFailure(page);
    await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");

    await cas.log("Log in attempt: #2");
    await submitLoginFailure(page);

    await cas.assertInnerText(page, "#content h2", "Access Denied");
    await cas.assertInnerText(page, "#content p", "You've entered the wrong password for the user too many times. You've been throttled.");

    await cas.log("Closing page and trying again with bad credentials...");
    await cas.closeBrowser(browser);

    browser = await cas.newBrowser(cas.browserOptions());
    page = await cas.newPage(browser);
    await cas.log("Log in attempt: #3");
    await submitLoginFailure(page);
    await cas.assertInnerText(page, "#content p", "You've entered the wrong password for the user too many times. You've been throttled.");
    await cas.closeBrowser(browser);
})();

async function submitLoginFailure(page) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, "casuser", "BadPassword1");
}

