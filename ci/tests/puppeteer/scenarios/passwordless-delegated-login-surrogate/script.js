
const cas = require("../../cas.js");

async function startAuthFlow(page, username) {
    await cas.log("Removing previous sessions and logging out");
    await cas.gotoLogout(page);
    
    await cas.log(`Starting authentication flow for ${username}`);
    await cas.gotoLogin(page);
    await cas.assertElementDoesNotExist(page, "#password");
    
    await cas.screenshot(page);
    await cas.type(page, "#username", username);
    await cas.sleep(1000);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(3000);
    await cas.logPage(page);
    await cas.screenshot(page);

    await cas.loginWith(page);
    await cas.sleep(7000);
    await cas.logPage(page);
    await cas.screenshot(page);
    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged in");
    await cas.click(page, "#auth-tab");
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.type(page, "#attribute-tab-1 input[type=search]", "surrogate");
    await cas.sleep(6000);
    await cas.screenshot(page);
    await cas.assertInnerTextStartsWith(page, "#surrogateEnabled td code kbd", "[true]");
    await cas.assertInnerTextStartsWith(page, "#surrogatePrincipal td code kbd", "[casuser]");
    await cas.assertInnerTextStartsWith(page, "#surrogateUser td code kbd", "[user3]");
    await cas.sleep(3000);
    await cas.screenshot(page);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await startAuthFlow(page, "user3+casuser-server");
    await startAuthFlow(page, "user3+casuser-none");
    await startAuthFlow(page, "user3+casuser-client");

    await cas.closeBrowser(browser);
})();
