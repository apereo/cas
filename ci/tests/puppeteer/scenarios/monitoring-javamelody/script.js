
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await page.authenticate({"username":"javamelody", "password": "M3ll0n"});
    let response = await cas.goto(page, "https://localhost:8443/cas/monitoring");
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    response = await cas.goto(page, "https://localhost:8443/cas/monitoring?part=counterSummaryPerClass&counter=spring");
    await cas.log(`${response.status()} ${response.statusText()}`);
    const entries = await cas.innerTexts(page, "td.wrappedText a");
    await cas.log(entries);

    assert(entries.find((entry) => entry === "ServicesManagerScheduledLoader") !== undefined);
    assert(entries.find((entry) => entry === "TicketRegistryCleanerScheduler") !== undefined);
    assert(entries.find((entry) => entry === "DefaultAuthenticationManager") !== undefined);
    assert(entries.find((entry) => entry === "DefaultAuthenticationManager") !== undefined);
    assert(entries.find((entry) => entry === "AcceptUsersAuthenticationHandler") !== undefined);
    assert(entries.find((entry) => entry === "DefaultTicketRegistry") !== undefined);
    await cas.closeBrowser(browser);
})();

