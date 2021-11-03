const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://apereo.github.io");

    await cas.loginWith(page, "casuser", "Mellon");
    const url = await page.url()
    console.log(`Page url: ${url}`)
    await cas.assertTicketParameter(page);

    await page.goto("https://localhost:8443/cas/login");
    const sessionCookie = (await page.cookies()).filter(value => {
        console.log(`Checking cookie ${value.name}`)
        return value.name === "SESSION"
    })[0];
    console.log(`Found session cookie ${sessionCookie.name}`)
    let cookieValue = await cas.base64Decode(sessionCookie.value);
    console.log(`Session cookie value ${cookieValue}`);
    await page.waitForTimeout(1000)
    await cas.doGet(`https://localhost:8443/cas/actuator/sessions/${cookieValue}`,
        function (res) {
        assert(res.data.id !== null);
        assert(res.data.creationTime !== null);
        assert(res.data.lastAccessedTime !== null);
        assert(res.data.attributeNames[0] === 'webflowConversationContainer');
        }, function (error) {
            throw error;
        }, { 'Content-Type': "application/json" })

    await browser.close();
})();
