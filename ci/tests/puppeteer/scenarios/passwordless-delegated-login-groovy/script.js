
const assert = require("assert");
const cas = require("../../cas.js");

async function submitUser(page, user) {
    await cas.type(page, "#username", user);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(2000);
}

(async () => {
    let failure = false;
    await cas.httpServer(__dirname, 5432, false);
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        await cas.log("Checking for invalid/unknown user account");
        await cas.gotoLogin(page);
        await submitUser(page, "none");
        await cas.assertInnerTextStartsWith(page, "#login div.banner",
            "Provided username cannot be recognized");

        await cas.log("Checking for password-eligible user account");
        await cas.gotoLogin(page);
        await submitUser(page, "local");
        await cas.assertInvisibility(page, "#loginProviders");
        await cas.assertVisibility(page, "#password");

        await cas.log("Checking for user account with a single delegated client");
        await cas.gotoLogin(page);
        await submitUser(page, "single-delegation");
        const url = await page.url();
        await cas.logPage(page);
        assert(url.startsWith("https://localhost:9859/anything/cas3"));

        await cas.log("Checking for all-options user account");
        await cas.gotoLogin(page);
        await submitUser(page, "all");
        await cas.assertVisibility(page, "#password");
        await cas.assertVisibility(page, "#loginProviders");
        await cas.assertVisibility(page, "li #CasClient2");
        await cas.assertVisibility(page, "li #CasClient3");
        await cas.assertInvisibility(page, "li #CasClient1");

        await cas.log("Checking for unauthorized use of identity provider");
        const response = await cas.goto(page, "https://localhost:8443/cas/clientredirect?client_name=CasClient3");
        await cas.log(`${response.status()} ${response.statusText()}`);
        assert(response.status() === 403);

        await cas.log("Checking for user account with multiple clients w/o password");
        await cas.gotoLogin(page);
        await submitUser(page, "multi-delegation");
        await cas.assertInvisibility(page, "#password");
        await cas.assertVisibility(page, "#loginProviders");
        await cas.assertVisibility(page, "li #CasClient2");
        await cas.assertVisibility(page, "li #CasClient3");
        await cas.assertInvisibility(page, "li #CasClient1");
    } catch (e) {
        failure = true;
        throw e;
    } finally {
        await browser.close();
        if (!failure) {
            await process.exit(0);
        }
    }
})();
