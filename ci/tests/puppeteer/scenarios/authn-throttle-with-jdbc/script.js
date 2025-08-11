
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Log in attempt: #1");
    await submitLoginFailure(page);
    await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");

    await cas.log("Log in attempt: #2");
    await submitLoginFailure(page);

    await cas.log("Log in attempt: #3");
    await submitLoginFailure(page);

    await cas.assertInnerText(page, "#content h2", "Access Denied");
    await cas.assertInnerTextContains(page, "#content p", "You've been throttled.");
    
    await cas.closeBrowser(browser);
})();

async function submitLoginFailure(page) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, "casuser", "BadPassword1");
}

