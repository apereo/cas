
const cas = require("../../cas.js");
const assert = require("assert");

async function gotoPage(page, instanceId, pageId) {
    const response = await cas.goto(page, `https://localhost:8443/cas/sba/instances/${instanceId}/${pageId}`);
    await cas.sleep(1000);
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/sba");
    await cas.screenshot(page);
    await cas.loginWith(page, "s#kiooritea", "p@$$W0rd");
    await cas.screenshot(page);
    await cas.sleep(3000);
    await cas.click(page, "div#CAS button");
    await cas.sleep(3000);
    await cas.click(page, "div#CAS li");
    await cas.sleep(3000);
    await cas.logPage(page);
    const url = await page.url();
    const pathArray = url.split("/");
    const instanceId = pathArray[pathArray.length - 2];
    await cas.log(`Instance ID ${instanceId}`);

    await gotoPage(page, instanceId, "metrics");
    await gotoPage(page, instanceId, "env");
    await gotoPage(page, instanceId, "beans");
    await gotoPage(page, instanceId, "configProps");

    await cas.closeBrowser(browser);
})();
