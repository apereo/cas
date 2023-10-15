const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');
const querystring = require("querystring");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.goto(page, "https://localhost:8443/cas/actuator/health");
    await page.waitForTimeout(1000);
    await cas.doGet("https://localhost:8443/cas/actuator/health",
        res => {
            assert(res.data.components.redis !== null);
            assert(res.data.components.memory !== null);
            assert(res.data.components.ping !== null);

            assert(res.data.components.redis.status !== null);
            assert(res.data.components.redis.details !== null);

        }, error => {
            throw error;
        }, { 'Content-Type': "application/json" });
    await browser.close();

    const baseUrl = "https://localhost:8443/cas/actuator/ssoSessions";
    await cas.logg("Removing all SSO Sessions");
    await cas.doRequest(`${baseUrl}?type=ALL&from=1&count=100000`, "DELETE", {});

    let formData = {
        username: 'casuser',
        password: 'Mellon'
    };
    let postData = querystring.stringify(formData);
    for (let i = 0; i < 50; i++) {
        await cas.doRequest('https://localhost:8443/cas/v1/tickets', "POST",
            {
                'Accept': 'application/json',
                'Content-Length': Buffer.byteLength(postData),
                'Content-Type': "application/x-www-form-urlencoded"
            },
            201, postData);
    }

    await cas.logg("Checking for SSO sessions for all users");
    await cas.doGet(`${baseUrl}?type=ALL`, res => {
        assert(res.status === 200);
    }, err => {
        throw err;
    });
    
})();





