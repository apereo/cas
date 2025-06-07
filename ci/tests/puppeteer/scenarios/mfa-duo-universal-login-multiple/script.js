
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await login(page, "mfa-duo", "https://localhost:9859/anything/cas");
    await cas.sleep(1000);

    await browser.close();
})();

async function login(page, providerId, service = undefined) {
    await cas.gotoLogout(page);
    await cas.sleep(1000);
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
        await cas.sleep(4000);
        await cas.logPage(page);
        const ticket = await cas.assertTicketParameter(page);
        const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
        await cas.log(body);
        const json = JSON.parse(body);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.attributes.authnContextClass === undefined);
    } else {
        await page.waitForSelector("#content", {visible: true});
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
        await cas.assertCookie(page);
        await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", providerId);
    }
}
