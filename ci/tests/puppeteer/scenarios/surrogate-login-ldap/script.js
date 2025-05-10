const cas = require("../../cas.js");
const assert = require("assert");

const SERVICE = "https://localhost:9859/anything/cas";

async function impersonate(page, surrogate, user) {
    await cas.gotoLogin(page, SERVICE);
    await cas.loginWith(page, `${surrogate}+${user}`);
    await cas.sleep(1000);
    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${SERVICE}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    const json = JSON.parse(body);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === surrogate);
    assert(authenticationSuccess.attributes.cn[0] === surrogate);
    assert(authenticationSuccess.attributes.uid[0] === surrogate);
    assert(authenticationSuccess.attributes.mail[0] === `${surrogate}@example.org`);
    assert(authenticationSuccess.attributes.surrogateUser[0] === surrogate);
    assert(authenticationSuccess.attributes.surrogateEnabled[0] === true);
    assert(authenticationSuccess.attributes.surrogatePrincipal[0] === user);
    await cas.gotoLogout(page);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await impersonate(page, "casperson1", "casuser");
    await cas.separator();
    await impersonate(page, "casperson2", "casuser");
    await cas.separator();
    await impersonate(page, "casperson1", "notcasuser");
    await cas.separator();
    await impersonate(page, "affiliate0", "casuser");
    await cas.separator();
    await impersonate(page, "affiliate0", "notcasuser");
    await browser.close();
})();
