
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.sleep(1000);

    const loginProviders = await page.$("#loginProviders");
    assert(loginProviders === null);

    await cas.gotoLogin(page, "https://localhost:9859/anything/sample");
    await cas.sleep(1000);

    await cas.assertVisibility(page, "li #CASServerOne");
    await cas.assertVisibility(page, "li #CASServerTwo");
    await cas.assertInvisibility(page, "#username");
    await cas.assertInvisibility(page, "#password");

    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices";
    await cas.doGet(baseUrl, (res) => {
        assert(res.status === 200);
        const length = res.data[1].length;
        cas.log(`Services found: ${length}`);
        assert(length === 2);
        res.data[1].forEach((service) => {
            assert(service.accessStrategy !== undefined);
            assert(service.accessStrategy.delegatedAuthenticationPolicy !== undefined);
        });
    }, (err) => {
        throw err;
    }, {
        "Content-Type": "application/json"
    });
    
    await cas.closeBrowser(browser);
})();

