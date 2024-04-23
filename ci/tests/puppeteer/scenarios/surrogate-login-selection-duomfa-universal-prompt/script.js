
const cas = require("../../cas.js");

(async () => {
    const body = {"configuredLevel": "INFO"};
    await ["org.apereo.cas", "org.springframework.webflow"].forEach((p) =>
        cas.doRequest(`https://localhost:8443/cas/actuator/loggers/${p}`, "POST",
            {"Content-Type": "application/json"}, 204, JSON.stringify(body, undefined, 2)));
    
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://example.org");

    await cas.loginWith(page, "+duobypass", "Mellon");
    await cas.sleep(1000);
    await cas.screenshot(page);
    
    await cas.assertTextContent(page, "#titlePanel h2", "Choose Account");
    await cas.assertTextContentStartsWith(page, "#surrogateInfo", "You are provided with a list of accounts");
    await cas.assertVisibility(page, "#surrogateTarget");
    await cas.assertVisibility(page, "#submit");
    await cas.assertVisibility(page, "#login");
    await page.select("#surrogateTarget", "user3");
    await cas.click(page, "#submit");
    await cas.waitForNavigation(page);
    await cas.screenshot(page);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged in");
    await cas.screenshot(page);
    await browser.close();
})();
