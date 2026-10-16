const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);
    const consumedExecution = await cas.elementValue(page, "input[name=execution]");

    await cas.loginWith(page);
    await cas.logPage(page);
    await cas.assertTicketParameter(page);

    await cas.log("Replaying a flow execution that has already completed must not log the user in");
    await cas.deleteCookies(page, "TGC");
    await cas.gotoLogin(page);
    await page.evaluate((execution, target) => {
        const form = document.createElement("form");
        form.method = "post";
        form.action = `/cas/login?service=${encodeURIComponent(target)}`;
        const fields = {username: "casuser", password: "Mellon", execution: execution, _eventId: "submit", geolocation: ""};
        Object.entries(fields).forEach(([name, value]) => {
            const input = document.createElement("input");
            input.type = "hidden";
            input.name = name;
            input.value = value;
            form.appendChild(input);
        });
        document.body.appendChild(form);
        form.submit();
    }, consumedExecution, service);
    await cas.sleep(3000);
    await cas.logPage(page);
    assert(page.url().startsWith("https://localhost:8443/cas/login"), `Replay must stay on the login page, but page is at ${page.url()}`);
    await cas.assertTicketParameter(page, false);
    await cas.assertVisibility(page, "#username");
    await cas.assertCookie(page, false);

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
        }, {"Content-Type": "application/json"});

    await cas.closeBrowser(browser);
})();
