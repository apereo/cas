
const cas = require("../../cas.js");
const assert = require("assert");

async function verifyAccountSelection(browser) {
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);
    await cas.gotoLoginWithLocale(page, "https://localhost:9859/anything/everything", "en");
    await cas.loginWith(page, "+casuser", "Mellon");
    await cas.assertTextContent(page, "#titlePanel h2", "Choose Account");
    await cas.assertTextContentStartsWith(page, "#surrogateInfo", "You are provided with a list of accounts");
    await cas.assertVisibility(page, "#surrogateTarget");
    await cas.assertVisibility(page, "#submit");
    await cas.assertInvisibility(page, "#cancel");
    await cas.assertVisibility(page, "#login");
    await cas.assertCookie(page, false);
    await context.close();
}

async function verifyImpersonationByPrincipalAttributes(browser) {
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);
    const service = "https://localhost:9859/anything/impersonation";
    await cas.gotoLoginWithLocale(page, service);
    await cas.loginWith(page, "impersonated+casuser", "Mellon");
    await cas.sleep(2000);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(service, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "impersonated");
    assert(authenticationSuccess.attributes.surrogateUser[0] === "impersonated");
    assert(authenticationSuccess.attributes.surrogateEnabled[0] === true);
    assert(authenticationSuccess.attributes.surrogatePrincipal[0] === "casuser");
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await context.close();
}

async function verifyImpersonationByPrincipalAttributesDisabled(browser) {
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);
    const service = "https://localhost:9859/anything/disabled";
    await cas.gotoLoginWithLocale(page, service);
    await cas.loginWith(page, "impersonated+casuser", "Mellon");
    await cas.sleep(2000);
    await cas.assertInnerTextStartsWith(page, "#loginErrorsPanel p", "You are not authorized to impersonate");
    await cas.assertCookie(page, false);
    await context.close();
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());

    await verifyAccountSelection(browser);
    await verifyImpersonationByPrincipalAttributes(browser);
    await verifyImpersonationByPrincipalAttributesDisabled(browser);
    
    await browser.close();
})();
