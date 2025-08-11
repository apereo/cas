
const cas = require("../../cas.js");

(async () => {
    let browser = await cas.newBrowser(cas.browserOptions());
    let page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    const eventId = await cas.elementValue(page, "input[name=_eventId]");
    const execution = await cas.elementValue(page, "input[name=execution]");
    const geolocation = await cas.elementValue(page, "input[name=geolocation]");
    await cas.log(`Event ID: ${eventId}, Execution: ${execution}, GeoLocation: ${geolocation}`);
    await cas.closeBrowser(browser);

    browser = await cas.newBrowser(cas.browserOptions());
    page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8444/cas/login");
    await cas.elementValue(page, "input[name=_eventId]", eventId);
    await cas.elementValue(page, "input[name=execution]", execution);
    await cas.elementValue(page, "input[name=geolocation]", geolocation);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.closeBrowser(browser);
})();
