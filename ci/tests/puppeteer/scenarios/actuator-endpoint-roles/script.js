const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);

    await cas.gotoLogin(page);
    await cas.loginWith(page);
    const endpoints = [
        "info",
        "health",
        "beans"
    ];
    
    const baseUrl = "https://localhost:8443/cas/actuator/";
    for (let i = 0; i < endpoints.length; i++) {
        const url = baseUrl + endpoints[i];
        await cas.log("===================================");
        await cas.log(`Trying ${url}`);

        const method = "GET";
        const body = await cas.doRequest(url, method, {
            "Authorization": `Basic ${btoa("casadmin:pa$$w0rd")}`,
            "Accept": "application/json",
            "Content-Type": "application/json",
            "User-Agent": "Chrome/51.0.2704.103 Safari/537.36"
        }, 200);
        await cas.log(body);
        await cas.log("===================================");
    }
    
    await context.close();
    await cas.closeBrowser(browser);
})();

