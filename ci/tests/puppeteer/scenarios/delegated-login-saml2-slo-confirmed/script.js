
const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);
    const service = "https://localhost:9859/anything/cas";

    await cas.sleep(2000);
    await cas.gotoLogin(page, service);
    await cas.sleep(2000);

    await cas.click(page, "li #SAML2Client");
    await cas.waitForNavigation(page);

    await cas.loginWith(page, "user1", "password");
    await cas.sleep(2000);

    const username = "user1@example.com";
    const ticket = await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.sleep(3000);
    const json = await cas.validateTicket(service, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user.includes(username));
    await cas.gotoLogout(page);
    await cas.sleep(2000);

    await cas.assertInnerText(page, "#content h2", `Do you, ${username}, want to log out completely?`);
    await cas.assertVisibility(page, "#logoutButton");
    await cas.assertVisibility(page, "#divServices");
    await cas.assertVisibility(page, "#servicesTable");
    await cas.submitForm(page, "#fm1");
    await cas.sleep(3000);
    await cas.logPage(page);
    await cas.assertInnerText(page, "#main-content h2", "Logout successful");
    await cas.assertInnerTextStartsWith(page, "#logoutMessage", "You have successfully logged out");
    await cas.assertCookie(page, false);
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await context.close();
    await browser.close();
})();

