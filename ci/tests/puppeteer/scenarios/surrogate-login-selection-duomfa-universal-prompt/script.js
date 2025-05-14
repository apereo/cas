
const cas = require("../../cas.js");
const assert = require("assert");

const service = "https://localhost:9859/anything/cas";

async function verifyImpersonationWithMfa(page) {
    await cas.gotoLogin(page, service);

    await cas.loginWith(page, "+duobypass", "Mellon");
    await cas.sleep(1000);
    await cas.screenshot(page);

    await cas.assertTextContent(page, "#titlePanel h2", "Choose Account");
    await cas.assertTextContentStartsWith(page, "#surrogateInfo", "You are provided with a list of accounts");
    await cas.assertVisibility(page, "#surrogateTarget");
    await cas.assertVisibility(page, "#submit");
    await cas.assertVisibility(page, "#login");
    await page.select("#surrogateTarget", "user3");
    await cas.click(page, "#submit");
    await cas.waitForNavigation(page);
    await cas.screenshot(page);
    const ticket = await cas.assertTicketParameter(page);

    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.logg(body);
    const json = JSON.parse(body.toString());
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "user3");
    assert(authenticationSuccess.attributes.surrogateEnabled[0] === true);
    assert(authenticationSuccess.attributes.surrogateUser[0] === "user3");
    assert(authenticationSuccess.attributes.surrogatePrincipal[0] === "duobypass");
    assert(authenticationSuccess.attributes.authnContextClass[0] === "mfa-duo");
    assert(authenticationSuccess.attributes.mail[0] === "user3@example.org");
    assert(authenticationSuccess.attributes.lastname[0] === "Three");
    assert(authenticationSuccess.attributes.uid[0] === "user3");
    assert(authenticationSuccess.attributes.phone[0] === "13477464500");

    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged in");
    await cas.screenshot(page);
    await cas.gotoLogout(page);
}

async function verifyNoImpersonationWithMfa(page) {
    await cas.updateDuoSecurityUserStatus("duocode");

    await cas.gotoLoginWithAuthnMethod(page, service);
    await cas.loginWith(page, "duocode", "Mellon");
    await cas.sleep(4000);
    const bypassCodes = await cas.fetchDuoSecurityBypassCodes("duocode");
    await cas.loginDuoSecurityBypassCode(page, "duocode", bypassCodes);
    await cas.sleep(4000);
    await cas.logPage(page);
    await cas.screenshot(page);
    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.logg(body);
    const json = JSON.parse(body.toString());
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "duocode");
    assert(authenticationSuccess.attributes.lastname[0] === "User");
    assert(authenticationSuccess.attributes.firstname[0] === "CAS");
    assert(authenticationSuccess.attributes.mail[0] === "casuser@example.org");
    assert(authenticationSuccess.attributes.authnContextClass[0] === "mfa-duo");
    assert(authenticationSuccess.attributes.uid[0] === "duocode");
    assert(authenticationSuccess.attributes.username[0] === "duocode");
    assert(authenticationSuccess.attributes.phone[0] === "13477464523");
    assert(authenticationSuccess.attributes.credentialType[0] === "DuoSecurityUniversalPromptCredential");
    assert(authenticationSuccess.attributes.authenticationMethod[0] === "DuoSecurityAuthenticationHandler");
    assert(authenticationSuccess.attributes.isFromNewLogin[0] === true);
    await cas.gotoLogout(page);
}

(async () => {
    const body = {"configuredLevel": "INFO"};
    await ["org.apereo.cas", "org.springframework.webflow"].forEach((p) =>
        cas.doRequest(`https://localhost:8443/cas/actuator/loggers/${p}`, "POST",
            {"Content-Type": "application/json"}, 204, JSON.stringify(body, undefined, 2)));
    
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await verifyImpersonationWithMfa(page);
    await verifyNoImpersonationWithMfa(page);
    
    await browser.close();
})();
