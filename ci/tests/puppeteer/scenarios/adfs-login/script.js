const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    let body = {"configuredLevel": "WARN"};
    await ["org.apereo.cas", "org.apereo.cas.web", "org.apereo.cas.web.flow"].forEach((p) =>
        cas.doRequest(`https://localhost:8443/cas/actuator/loggers/${p}`, "POST",
            {"Content-Type": "application/json"}, 204, JSON.stringify(body, undefined, 2)));
    const service = "https://localhost:9859/anything/cas";
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.log(`Navigating to ${service}`);
    await cas.gotoLogin(page, service);
    await cas.waitForTimeout(page);
    await cas.click(page, "div .idp span");
    await cas.waitForTimeout(page, 4000);
    await cas.screenshot(page);
    await cas.type(page, "#userNameInput", process.env.ADFS_USERNAME, true);
    await cas.type(page, "#passwordInput", process.env.ADFS_PASSWORD, true);
    await cas.waitForTimeout(page);
    await cas.screenshot(page);
    await cas.submitForm(page, "#loginForm");
    await cas.waitForTimeout(page, 4000);
    await cas.screenshot(page);
    const ticket = await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.waitForTimeout(page, 3000);
    body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    const json = JSON.parse(body);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user.includes("casuser@apereo.org"));
    assert(authenticationSuccess.attributes.firstname !== undefined);
    assert(authenticationSuccess.attributes.lastname !== undefined);
    assert(authenticationSuccess.attributes.uid !== undefined);
    assert(authenticationSuccess.attributes.upn !== undefined);
    assert(authenticationSuccess.attributes.username !== undefined);
    assert(authenticationSuccess.attributes.surname !== undefined);
    assert(authenticationSuccess.attributes.email !== undefined);
    await browser.close();
})();
