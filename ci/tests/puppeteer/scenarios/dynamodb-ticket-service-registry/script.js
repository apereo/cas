
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.goto(page, "https://localhost:8443/cas/actuator/health");
    await cas.sleep(1000);
    await cas.doGet("https://localhost:8443/cas/actuator/health",
        () => {

        }, (error) => {
            throw error;
        }, { "Content-Type": "application/json" });
    await cas.closeBrowser(browser);
})();
