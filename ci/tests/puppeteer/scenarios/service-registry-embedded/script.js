
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page);
    await page.url();
    await cas.logPage(page);
    await cas.assertTicketParameter(page);
    await browser.close();

    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices";
    await cas.doGet(baseUrl, (res) => {
        assert(res.status === 200);
        const length = res.data[1].length;
        cas.log(`Services found: ${length}`);
        assert(length === 1);
        res.data[1].forEach((service) => {
            assert(service.id === 1);
            assert(service.name === "Sample");
        });
    }, (err) => {
        throw err;
    }, {
        "Content-Type": "application/json"
    });
})();
