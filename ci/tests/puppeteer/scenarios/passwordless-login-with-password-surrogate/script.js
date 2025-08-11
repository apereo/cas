
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page);

    await cas.assertElementDoesNotExist(page, "#password");

    await cas.type(page,"#username", "user3+casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.sleep(2000);

    await cas.assertInvisibility(page, "#username");
    await cas.assertVisibility(page, "#password");

    await cas.type(page,"#password", "Mellon");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.sleep(2000);

    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged in");

    await cas.click(page, "#auth-tab");
    await cas.sleep(1000);
    await cas.type(page, "#attribute-tab-1 input[type=search]", "surrogate");
    await cas.sleep(1000);
    await cas.screenshot(page);
    
    await cas.assertInnerTextStartsWith(page, "#surrogateEnabled td code kbd", "[true]");
    await cas.assertInnerTextStartsWith(page, "#surrogatePrincipal td code kbd", "[casuser]");
    await cas.assertInnerTextStartsWith(page, "#surrogateUser td code kbd", "[user3]");
    await cas.sleep(1000);

    await cas.closeBrowser(browser);
})();
