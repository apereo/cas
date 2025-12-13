const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.isVisible(page, "#clientIpAddress");
    const clientIp = await cas.innerText(page, "#clientIpAddress");
    await cas.logb(clientIp);
    await cas.closeBrowser(browser);

    const url = "https://localhost:8443/cas/actuator/info";
    await cas.doGet(url,
        () => {
            throw "Should not have been able to access actuator info endpoint";
        }, (err) => {
            cas.logg(`Access to actuator info endpoint is correctly denied: ${err.status}`);
        });

    await cas.doGet(url,
        () => {
            throw "Should not have been able to access actuator info endpoint";
        }, (err) => {
            cas.logg(`Access to actuator info endpoint cannot be spoofed: ${err.status}`);
        }, {
            "Content-Type": "application/json",
            "X-Forwarded-For": "192.0.0.1"
        });
})();
