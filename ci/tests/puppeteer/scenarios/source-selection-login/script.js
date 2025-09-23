const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";
    
    await cas.gotoLogin(page);
    await cas.assertVisibility(page, "#authnSourceSection");
    await cas.assertInnerText(page, "#JSON-authnSource", "JSON");
    await cas.assertInnerText(page, "#EXAMPLE-authnSource", "EXAMPLE");
    await cas.assertInnerText(page, "#REJECT-authnSource", "REJECT");
    await cas.sleep(1000);

    await cas.gotoLogin(page, service);
    await cas.sleep(1000);

    await cas.log("Switching to REJECT authentication source");
    await cas.elementValue(page, "#source", "REJECT");
    await cas.log("Authentication is expected to fail and rejected");
    await cas.loginWith(page, "json", "json");
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.assertTicketParameter(page, false);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/login");
    await cas.assertCookie(page, false);

    await cas.log("Switching to REJECT authentication source");
    await cas.elementValue(page, "#source", "JSON");
    await cas.loginWith(page, "json", "json");
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.assertTicketParameter(page, true);
    
    await cas.closeBrowser(browser);
})();
