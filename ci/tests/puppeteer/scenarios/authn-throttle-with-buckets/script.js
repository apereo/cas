
const cas = require("../../cas.js");

(async () => {
    let browser = await cas.newBrowser(cas.browserOptions());
    let page = await cas.newPage(browser);

    await cas.logg("Log in attempt: #0 Successful Login");
    await submitLogin(page, "casuser", "Mellon");
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
    await cas.sleep(2000);

    await cas.log("Log in attempt: #1");
    await submitLogin(page);

    await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");

    await cas.log("Log in attempt: #2");
    await submitLogin(page);

    await cas.assertInnerText(page, "#content h2", "Access Denied");
    await cas.assertInnerText(page, "#content p", "You've entered the wrong password for the user too many times. You've been throttled.");

    await cas.log("Closing browser and trying again with bad credentials...");
    await browser.close();

    browser = await cas.newBrowser(cas.browserOptions());
    page = await cas.newPage(browser);
    await cas.log("Log in attempt: #2");
    await submitLogin(page);
    await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");
    await browser.close();
})();

async function submitLogin(page, user = "casuser", password = "BadPassword1") {
    await cas.gotoLogin(page);
    await cas.sleep(500);
    await cas.loginWith(page, user, password);
    await cas.sleep(500);
}

