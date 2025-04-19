
const cas = require("../../cas.js");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const service = "https://localhost:8989/realms/cas/account";
    await cas.goto(page, service);
    await cas.sleep(1000);
    await cas.doRequest("https://localhost:8443/cas/sp/metadata", "GET", {}, 200);
    await cas.doRequest("https://localhost:8443/cas/sp/idp/metadata", "GET", {}, 200);

    await cas.sleep(1000);
    await cas.click(page, "#social-saml");
    await cas.sleep(3000);
    await cas.assertVisibility(page, "li #SAML2Client");

    await cas.click(page, "li #SAML2Client");
    await cas.waitForNavigation(page);
    await cas.loginWith(page, "user1", "password");
    await cas.sleep(4000);
    await cas.screenshot(page);
    await cas.logPage(page);

    await cas.assertInputValue(page, "#username", "user1@example.com");
    await cas.assertInputValue(page, "#email", "user1@example.com");
    await cas.assertInputValue(page, "#firstName", "CAS");
    await cas.assertInputValue(page, "#lastName", "Apereo");
    
    await cas.sleep(2000);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user1@example.com, have successfully logged in");
    await cas.sleep(1000);

    await cas.goto(page, service);
    await cas.sleep(1000);
    await cas.logb("Logging out...");
    await cas.click(page, "button.pf-v5-c-menu-toggle");
    await cas.sleep(1000);
    await cas.click(page, "button[role=menuitem]");
    await cas.sleep(5000);
    await cas.logPage(page);

    await cas.assertPageUrlStartsWith(page, "https://localhost:8989/realms/cas");
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");
    await cas.assertVisibility(page, "#social-saml");
    await cas.sleep(2000);
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await browser.close();
})();
