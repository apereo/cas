
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.sleep(3000);

    await cas.assertVisibility(page, "#loginProviders");
    await cas.assertVisibility(page, "li #TwitterClient");
    await cas.assertVisibility(page, "li #GitHubClient");

    await cas.goto(page, "https://localhost:8443/cas/clientredirect?client_name=GithubClient");
    await cas.sleep(3000);

    await cas.assertVisibility(page, "#login_field");

    await cas.closeBrowser(browser);
})();
