
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const service = "https://localhost:9859/anything/unknown";

    let response = await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() >= 400);

    response = await cas.gotoLogin(page, `${service}&client_id=client`);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() >= 400);

    await cas.log("Checking for unauthorized logout redirect...");
    response = await cas.goto(page, `https://localhost:8443/cas/logout?client_id=client&service=${service}`);
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.status() >= 400);
    
    await browser.close();
})();
