const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Create SSO session with external CAS server...");
    await cas.goto(page, "https://localhost:8444/cas/login");
    await cas.screenshot(page);
    await cas.loginWith(page);

    await cas.assertCookie(page, true, "TGCEXT");

    await cas.log("Start with first application without SSO for CAS server");
    await cas.goto(page, "https://localhost:8443/cas/clientredirect?locale=de&client_name=CASServer&service=https://github.com/apereo/cas");

    await cas.assertTicketParameter(page);

    await cas.log("Checking SSO for our CAS server");
    await cas.gotoLogin(page);

    await cas.assertCookie(page);
    await cas.assertInnerText(page, "#content div h2", "Anmeldung erfolgreich");

    await browser.close();
})();
