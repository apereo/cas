const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/palantir/dashboard");
    let response = await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    await cas.sleep(2000);
    await cas.screenshot(page);
    assert(response.status() === 200);
    response = await cas.goto(page, "https://localhost:8443/cas/palantir/dashboard");
    await cas.log(`${response.status()} ${response.statusText()}`);
    await cas.screenshot(page);
    assert(response.ok());

    await cas.click(page, "#palantirLogoutButton");
    await cas.sleep(2000);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/logout");
    await cas.assertCookie(page, false);
    await cas.closeBrowser(browser);
})();
