
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page);
    await cas.logPage(page);
    await cas.assertTicketParameter(page);

    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices";
    await cas.doGet(baseUrl, (res) => {
        assert(res.status === 200);
        cas.log(`Services found: ${res.data[1].length}`);

        res.data[1].forEach((svc) => {
            cas.log(`Checking service ${svc.name}-${svc.id}`);
            assert(svc.description === "My Application");
            assert(svc.attributeReleasePolicy.allowedAttributes[1].includes("email"));
            assert(svc.attributeReleasePolicy.allowedAttributes[1].includes("username"));
        });
        
    }, (err) => {
        throw err;
    }, {
        "Content-Type": "application/json"
    });
    
    await browser.close();
})();
