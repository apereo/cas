const cas = require("../../cas.js");

const assert = require("assert");

(async () => {
    const casLoginUrl = "https://localhost:4443/cas/login";
    await cas.doRequest(casLoginUrl, "GET", {}, 401);
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.authenticate({"username":"admin", "password": "password"});
    const response = await cas.goto(page, casLoginUrl);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    await cas.assertCookie(page);
    await cas.closeBrowser(browser);
})();
