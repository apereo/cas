const puppeteer = require("puppeteer");
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://apereo.github.io";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page, "casuser", "TheBestPasswordEver");
    const ticket = await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await page.waitForTimeout(1000);
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    const json = JSON.parse(body);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes["first-name"] !== null);
    assert(authenticationSuccess.attributes["last-name"] !== null);
    assert(authenticationSuccess.attributes["phonenumber"] !== null);

    await browser.close();
})();
