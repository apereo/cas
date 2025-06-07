
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8444");
    await cas.sleep(1000);
    await cas.goto(page, "https://localhost:8444/protected");
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8444/protected");
    await cas.assertInnerTextContains(page, "div.starter-template h2 span", "casuser");
    await cas.gotoLogout(page);
    await cas.sleep(2000);
    await cas.goto(page, "https://localhost:8444/protected");
    await cas.sleep(3000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/login?service=");
    await browser.close();
})();
