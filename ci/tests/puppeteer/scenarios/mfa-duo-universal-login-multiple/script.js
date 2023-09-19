const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    // await login(page, "mfa-duo");
    // await page.waitForTimeout(1000)
    // await login(page, "mfa-duo-alt");
    // await page.waitForTimeout(1000)

    await login(page, "mfa-duo", "https://apereo.github.io");
    await page.waitForTimeout(1000);

    await browser.close();
})();

async function login(page, providerId, service = undefined) {
    await cas.goto(page, "https://localhost:8443/cas/logout");
    await page.waitForTimeout(1000);
    await cas.assertCookie(page, false);

    await cas.log(`Trying with provider id ${providerId} and service ${service}`);
    let url = `https://localhost:8443/cas/login?authn_method=${providerId}`;
    if (service !== undefined) {
        url += `&service=${service}`;
    }
    await cas.goto(page, url);
    await cas.loginWith(page, "duobypass", "Mellon");
    await cas.screenshot(page);
    if (service !== undefined) {
        await page.waitForTimeout(4000);
        const url = await page.url();
        await cas.log(`Page url: ${url}`);
        let ticket = await cas.assertTicketParameter(page);
        let body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
        await cas.log(body);
        let json = JSON.parse(body);
        let authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.attributes.authnContextClass === undefined);
    } else {
        await page.waitForSelector("#content", {visible: true});
        await cas.assertInnerText(page, '#content div h2', "Log In Successful");
        await cas.assertCookie(page);
        await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", providerId);
    }
}
