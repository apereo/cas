
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");

    await cas.loginWith(page);
    await cas.logPage(page);
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page);
    const sessionCookie = await cas.containsCookie(page, "SESSION");
    await cas.log(`Found session cookie ${sessionCookie.name}`);
    const cookieValue = await cas.base64Decode(sessionCookie.value);
    await cas.log(`Session cookie value ${cookieValue}`);
    await cas.sleep(1000);
    await cas.doGet(`https://localhost:8443/cas/actuator/sessions/${cookieValue}`,
        (res) => {
            assert(res.data.id !== undefined);
            assert(res.data.creationTime !== undefined);
            assert(res.data.lastAccessedTime !== undefined);
            assert(res.data.attributeNames[0] === "webflowConversationContainer");
        }, (error) => {
            throw error;
        }, { "Content-Type": "application/json" });

    await cas.closeBrowser(browser);
})();
