
const assert = require("assert");
const cas = require("../../cas.js");

async function startAuthFlow(page, username) {
    await cas.log("Removing previous sessions and logging out");
    await cas.gotoLogout(page);
    await cas.log(`Starting authentication flow for ${username}`);
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertElementDoesNotExist(page, "#password");
    await cas.type(page, "#username", username);
    await cas.pressEnter(page);
    await cas.sleep(5000);
    await cas.screenshot(page);
    await cas.logPage(page);

    let expectedUser = "casuser";
    if (username.endsWith("-saml")) {
        expectedUser = "user1@example.com";
        await cas.loginWith(page, "user1", "password");
    } else {
        await cas.assertPageUrlStartsWith(page, "https://localhost:8444");
        await cas.sleep(1000);
        await cas.loginWith(page);
    }
    await cas.sleep(5000);
    await cas.logPage(page);
    await cas.assertCookie(page);
    
    await cas.assertInnerTextStartsWith(page, "#content div p", `You, ${expectedUser}, have successfully logged in`);

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
    const users = ["casuser-server", "casuser-none", "casuser-client", "casuser-saml"];
    for (const user of users) {
        const context = await browser.createBrowserContext();
        const page = await cas.newPage(context);
        await startAuthFlow(page, user);
        await context.close();
    }
    await browser.close();
})();
