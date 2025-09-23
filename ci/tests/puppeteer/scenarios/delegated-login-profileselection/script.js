
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.assertVisibility(page, "li #CasClient");
    await cas.click(page, "li #CasClient");
    await cas.waitForNavigation(page);

    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.loginWith(page);
    await cas.sleep(4000);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#content div h2", "Delegated Authentication Profile Selection");
    await cas.assertPageTitleContains(page, "Delegated Authentication Profile Selection");

    const profileId = await cas.innerText(page, "#profilesTable tr td code");
    await cas.log(profileId);
    await cas.submitForm(page, `#profilesTable #form-${profileId}`);
    await cas.sleep(6000);
    await cas.assertCookie(page);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    const principalId = await cas.innerText(page, "span#principalId");
    assert(principalId === profileId);
    
    await cas.closeBrowser(browser);
})();
