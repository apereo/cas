
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.sleep(5000);

    await cas.assertVisibility(page, "#loginProviders");
    await cas.assertVisibility(page, "li #TwitterClient");
    await cas.assertVisibility(page, "li #CasClient");
    await cas.assertVisibility(page, "li #GitHubClient");

    await cas.goto(page, "https://localhost:8443/cas/login?error=Fail&error_description=Error&error_code=400&error_reason=Reason");
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content div h2", "Unauthorized Access");
    await cas.assertTextContentStartsWith(page, "#content div p", "Either the authentication request was rejected/cancelled");

    await cas.assertVisibility(page, "#errorTable");
    await cas.assertVisibility(page, "#loginLink");
    await cas.assertVisibility(page, "#appLink");

    await browser.close();
})();
