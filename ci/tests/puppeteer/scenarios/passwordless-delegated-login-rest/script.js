
const cas = require("../../cas.js");

async function submitUser(page, user) {
    await cas.type(page, "#username", user);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
}

(async () => {
    let failure = false;
    await cas.httpServer(__dirname, 5432, false);
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        await cas.log("Checking for invalid/unknown user account");
        await cas.gotoLogin(page);
        await submitUser(page, "unknown");
        await cas.assertInnerTextStartsWith(page, "#login div.banner",
            "Provided username cannot be recognized");

        await cas.log("Checking for password-eligible user account");
        await cas.gotoLogin(page);
        await submitUser(page, "local.json");
        await cas.assertInvisibility(page, "#loginProviders");
        await cas.assertVisibility(page, "#password");

        await cas.log("Checking for user account with a single delegated client");
        await cas.gotoLogin(page);
        await submitUser(page, "single-delegation.json");
        await cas.logPage(page);
        await cas.assertPageUrlStartsWith(page, "https://localhost:9859/anything/cas3");

        await cas.log("Checking for all-options user account");
        await cas.gotoLogin(page);
        await submitUser(page, "all.json");
        await cas.assertVisibility(page, "#password");
        await cas.assertVisibility(page, "#loginProviders");
        await cas.assertVisibility(page, "li #CasClient2");
        await cas.assertInvisibility(page, "li #CasClient3");
        await cas.assertVisibility(page, "li #CasClient1");
  
        await cas.log("Checking for user account with multiple clients w/o password");
        await cas.gotoLogin(page);
        await submitUser(page, "multi-delegation.json");
        await cas.assertInvisibility(page, "#password");
        await cas.assertVisibility(page, "#loginProviders");
        await cas.assertVisibility(page, "li #CasClient2");
        await cas.assertVisibility(page, "li #CasClient3");
        await cas.assertInvisibility(page, "li #CasClient1");
    } catch (e) {
        failure = true;
        throw e;
    } finally {
        await cas.closeBrowser(browser);
        if (!failure) {
            await process.exit(0);
        }
    }
})();
