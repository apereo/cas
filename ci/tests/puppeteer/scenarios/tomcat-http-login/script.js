
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "http://localhost:8080/cas/login");
    await cas.loginWith(page);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page);
    await cas.goto(page, "http://localhost:8080/cas/logout");
    await cas.assertCookie(page, false);

    await cas.closeBrowser(browser);
})();
