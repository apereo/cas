
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://host.k3d.internal/sessions/new?return_to=%2F");
    await cas.sleep(10000);
    await cas.assertPageTitle(page, "SonarQube");
    await cas.assertInnerText(page, "h1.login-title","Log in to SonarQube");
    //await cas.sleep(5000);
    await cas.click(page,"a.identity-provider-link");
    //await cas.sleep(5000);
    await cas.loginWith(page);
    //await cas.sleep(5000);
    // hit strapi endpoint that triggers CAS login to get JWT
    //await cas.goto(page, "https://host.k3d.internal");
    await cas.sleep(3000);
    await cas.assertInnerTextContains(page, "ul.global-navbar-menu","Projects");
    await cas.closeBrowser(browser);
})();
