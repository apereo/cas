const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    await cas.refreshContext();

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?" +
        "client_id=client&" +
        "redirect_uri=https://localhost:9859/anything/oidc&" +
        `scope=${encodeURIComponent("openid email profile address phone")}&` +
        "response_type=code";
    await cas.goto(page, url);

    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.click(page, "#allow");
    await cas.waitForNavigation(page);
    await cas.sleep(1000);

    const content = await cas.textContent(page, "body");
    const payload = JSON.parse(content);
    await cas.log(payload);
    assert(payload.args.code !== undefined);

    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices";
    await cas.doGet(baseUrl, (res) => {
        assert(res.status === 200);
        const length = res.data[1].length;
        cas.log(`Services found: ${length}`);
        assert(length === 2);
    }, (err) => {
        throw err;
    }, {
        "Content-Type": "application/json"
    });

    await cas.closeBrowser(browser);

})();
