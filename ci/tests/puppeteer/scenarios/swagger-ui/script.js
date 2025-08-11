
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    let response = await cas.goto(page, "https://localhost:8443/cas/v3/api-docs");
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    response = await cas.goto(page, "https://localhost:8443/cas/swagger-ui/index.html");
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await cas.closeBrowser(browser);
})();
