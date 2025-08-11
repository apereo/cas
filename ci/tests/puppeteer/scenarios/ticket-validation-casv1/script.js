const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);
    await cas.attributeValue(page, "#username", "autocapitalize", "none");
    await cas.attributeValue(page, "#username", "spellcheck", "false");
    await cas.attributeValue(page, "#username", "autocomplete", "username");
    await cas.loginWith(page);
    const ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/validate?service=${service}&ticket=${ticket}`);
    await cas.log(body);
    assert(body === "yes\ncasuser\n");
    await cas.closeBrowser(browser);
})();
