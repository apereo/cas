const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());

    const page = await cas.newPage(browser);
    for (let i = 0; i < 5; i++) {
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.assertCookie(page);
        await cas.gotoLogout(page);
        await cas.assertCookie(page, false);
    }
    await cas.goto(page, "https://localhost:8443/cas/actuator/registeredServices");
    await cas.waitForTimeout(page, 1000);
    await browser.close();
    const baseUrl = "https://localhost:8443/cas/actuator";
    await cas.doRequest(`${baseUrl}/metrics`, "GET", {
        "Accept": "application/json", "Content-Type": "application/json"
    }, 200);
    await cas.doRequest(`${baseUrl}/prometheus`, "GET", {}, 200);
})();

