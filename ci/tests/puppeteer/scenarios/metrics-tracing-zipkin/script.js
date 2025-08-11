
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.gotoLogout(page);

    const webflowMetrics = [ "login", "logout" ];
    const baseUrl = "https://localhost:8443/cas/actuator";
    for (let i = 0; i < webflowMetrics.length; i++) {
        const url = `${baseUrl}/metrics/org.springframework.webflow.executor.FlowExecutor.${webflowMetrics[i]}`;
        await cas.log(`Trying ${url}`);
        await cas.doRequest(url, "GET", {
            "Accept": "application/json",
            "Content-Type": "application/json"
        }, 200);
    }
    await cas.goto(page, "http://localhost:9411/zipkin");
    await cas.sleep(4000);
    await cas.closeBrowser(browser);
})();
