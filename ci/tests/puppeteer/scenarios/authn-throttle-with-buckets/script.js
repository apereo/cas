const cas = require("../../cas.js");

(async () => {
    let browser = await cas.newBrowser(cas.browserOptions());
    let page = await cas.newPage(browser);

    await cas.logg("Log in attempt: #0 Successful Login");
    await submitLogin(page, "casuser", "Mellon");
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
    await cas.sleep(1000);

    await cas.log("Log in attempt: #1");
    await submitLogin(page);

    await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");

    for (let i = 2; i < 6; i++) {
        await cas.log(`Log in attempt: #${i}`);
        const response = await submitLogin(page);
        await cas.log(`${response.status()} ${response.statusText()}`);
        const status = response.status();
        if (status === 401) {
            await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");
        } else if (status === 423) {
            await cas.assertInnerText(page, "#content h2", "Access Denied");
            await cas.assertInnerText(page, "#content p", "You've entered the wrong password for the user too many times. You've been throttled.");
        }
    }

    await cas.log("Closing browser and trying again with bad credentials...");
    await cas.closeBrowser(browser);

    browser = await cas.newBrowser(cas.browserOptions());
    page = await cas.newPage(browser);
    await cas.log("Log in attempt: #2");
    const response = await submitLogin(page);
    const status = response.status();
    if (status === 401) {
        await cas.assertInnerTextStartsWith(page, "#content div.banner p", "Authentication attempt has failed");
    } else if (status === 423) {
        await cas.assertInnerText(page, "#content h2", "Access Denied");
        await cas.assertInnerText(page, "#content p", "You've entered the wrong password for the user too many times. You've been throttled.");
    }
    await cas.closeBrowser(browser);
})();

async function submitLogin(page, user = "casuser", password = "BadPassword1") {
    await cas.gotoLogin(page);
    await cas.sleep(500);
    return cas.loginWith(page, user, password, "#username", "#password", 8000);
}

