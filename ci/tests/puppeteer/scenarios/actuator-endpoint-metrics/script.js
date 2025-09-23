const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());

    for (let i = 0; i < 5; i++) {
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.sleep(2000);
        await cas.assertCookie(page);
        await cas.gotoLogout(page);
        await cas.sleep(2000);
        await cas.assertCookie(page, false);
        await cas.goto(page, "https://localhost:8443/cas/actuator/registeredServices");
        await cas.sleep(1500);
    }
    await cas.closeBrowser(browser);
    const baseUrl = "https://localhost:8443/cas/actuator";
    await cas.doRequest(`${baseUrl}/metrics`, "GET", {
        "Accept": "application/json", "Content-Type": "application/json"
    }, 200);
    await cas.doRequest(`${baseUrl}/prometheus`, "GET", {}, 200);
})();

