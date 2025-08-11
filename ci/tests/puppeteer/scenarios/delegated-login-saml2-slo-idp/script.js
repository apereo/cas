const cas = require("../../cas.js");
const path = require("path");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8444");
    await cas.sleep(1000);

    await cas.log("Accessing protected CAS application");
    await cas.goto(page, "https://localhost:8444/protected");
    await cas.logPage(page);

    await cas.sleep(2000);
    await cas.screenshot(page);

    await cas.assertVisibility(page, "#loginProviders");
    await cas.assertVisibility(page, "li #SAML2Client");

    await cas.log("Choosing SAML2 identity provider for login...");
    await cas.click(page, "li #SAML2Client");
    await cas.waitForNavigation(page);

    await cas.loginWith(page, "user1", "password");
    await cas.sleep(4000);

    await cas.log("Checking CAS application access...");
    await cas.logPage(page);
    await cas.screenshot(page);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8444/protected");
    await cas.assertInnerTextContains(page, "div.starter-template h2 span", "user1@example.com");

    await cas.log("Checking CAS SSO session...");
    await cas.gotoLogin(page);
    await cas.screenshot(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page, true, "Pac4jCookie");
    await cas.sleep(3000);

    await cas.log("Invoking SAML2 identity provider SLO...");
    await cas.goto(page, "http://localhost:9443/simplesaml/saml2/idp/SingleLogoutService.php?ReturnTo=https://apereo.github.io");
    await cas.sleep(5000);
    await cas.logPage(page);
    await cas.screenshot(page);
    await cas.assertPageUrlStartsWith(page, "https://apereo.github.io");

    await cas.sleep(1000);
    await cas.gotoLogin(page);
    await cas.assertCookie(page, false);

    await cas.log("Accessing protected CAS application");
    await cas.goto(page, "https://localhost:8444/protected");
    await cas.sleep(3000);
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.screenshot(page);
    await cas.assertPageUrlStartsWith(page, "http://localhost:9443/simplesaml");
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");
    const title = await page.title();
    await cas.log(title);
    assert(title === "Enter your username and password");
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();

