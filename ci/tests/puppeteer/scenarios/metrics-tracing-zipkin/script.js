
const assert = require("assert");
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
    await cas.sleep(4000);
    await cas.doGet("http://localhost:9411/api/v2/services",
        (res) => {
            assert(res.status === 200);
            assert(res.data.includes("cas"));
        },
        (error) => {
            throw error;
        });
    await cas.doGet("http://localhost:9411/api/v2/traces?serviceName=cas&limit=10",
        (res) => {
            assert(res.status === 200);
            assert(res.data.length > 0);
        },
        (error) => {
            throw error;
        });
    await cas.closeBrowser(browser);
})();
