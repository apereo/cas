
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);

    await cas.doRequest("https://localhost:8443/cas/sp/metadata", "GET", {}, 200);
    await cas.doRequest("https://localhost:8443/cas/sp/idp/metadata", "GET", {}, 200);

    await cas.assertVisibility(page, "li #keycloak");
    await cas.click(page, "li #keycloak");
    await cas.sleep(3000);
    await cas.screenshot(page);
    await cas.loginWith(page, "caskeycloak", "r2RlZXz6f2h5");
    await cas.sleep(2000);
    await cas.logPage(page);

    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    const json = JSON.parse(body);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "caskeycloak@example.org");
    assert(authenticationSuccess.attributes.name[0] === "CAS");
    assert(authenticationSuccess.attributes.department[0] === "SSO");

    await cas.sleep(2000);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, caskeycloak@example.org, have successfully logged in");

    await cas.logb("Logging out...");
    await cas.gotoLogout(page, service);
    await cas.sleep(6000);
    await cas.logPage(page);
    await cas.sleep(3000);
    await cas.assertPageUrlStartsWith(page, service);
    await cas.gotoLogin(page, service);
    await cas.click(page, "li #keycloak");
    await cas.sleep(3000);
    await browser.close();
})();
