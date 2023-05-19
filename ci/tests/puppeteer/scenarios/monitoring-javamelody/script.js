const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await page.authenticate({'username':'javamelody', 'password': 'M3ll0n'});
    let response = await cas.goto(page, "https://localhost:8443/cas/monitoring");
    console.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    response = await cas.goto(page, "https://localhost:8443/cas/monitoring?part=counterSummaryPerClass&counter=spring");
    console.log(`${response.status()} ${response.statusText()}`);
    let entries = await cas.innerTexts(page, "td.wrappedText a");
    console.log(entries);

    assert(entries.find(entry => entry === "ServicesManagerScheduledLoader") !== undefined);
    assert(entries.find(entry => entry === "TicketRegistryCleanerScheduler") !== undefined);
    assert(entries.find(entry => entry === "DefaultAuthenticationManager") !== undefined);
    assert(entries.find(entry => entry === "DefaultAuthenticationManager") !== undefined);
    assert(entries.find(entry => entry === "AcceptUsersAuthenticationHandler") !== undefined);
    assert(entries.find(entry => entry === "DefaultTicketRegistry") !== undefined);
    await browser.close();
})();

