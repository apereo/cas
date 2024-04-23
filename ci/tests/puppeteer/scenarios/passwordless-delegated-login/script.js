
const assert = require("assert");
const cas = require("../../cas.js");

async function startAuthFlow(page, username) {
    await cas.log("Removing previous sessions and logging out");
    await cas.gotoLogout(page);
    await cas.log(`Starting authentication flow for ${username}`);
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    const pswd = await page.$("#password");
    assert(pswd === null);
    await cas.type(page, "#username", username);
    await cas.pressEnter(page);
    await cas.sleep(9000);
    await cas.screenshot(page);
    await cas.logPage(page);
    const url = await page.url();
    assert(url.startsWith("https://localhost:8444"));
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(9000);
    await cas.logPage(page);
    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, casuser, have successfully logged in");

    await cas.click(page, "#auth-tab");
    await cas.sleep(1000);
    await cas.type(page, "#attribute-tab-1 input[type=search]", "surrogate");
    await cas.sleep(1000);
    await cas.screenshot(page);

    const surrogateEnabled = await page.$("#surrogateEnabled");
    assert(surrogateEnabled === null);
    const surrogatePrincipal = await page.$("#surrogatePrincipal");
    assert(surrogatePrincipal === null);
    const surrogateUser = await page.$("#surrogateUser");
    assert(surrogateUser === null);
    await cas.sleep(1000);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await startAuthFlow(page, "casuser-server");
    await startAuthFlow(page, "casuser-none");
    await startAuthFlow(page, "casuser-client");

    await browser.close();
})();
