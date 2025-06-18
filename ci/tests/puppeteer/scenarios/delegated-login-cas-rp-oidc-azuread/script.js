
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";
    
    const url = `https://localhost:8443/cas/login?service=${service}`;
    await cas.goto(page, url);

    await cas.assertVisibility(page, "li #AzureClient");
    await cas.click(page, "li #AzureClient");
    await cas.waitForNavigation(page);
    await cas.sleep(4000);
    await cas.screenshot(page);

    const username = `castest@${process.env.AZURE_AD_DOMAIN}`;
    await cas.type(page, "input[name=loginfmt]", username, true);
    await cas.pressEnter(page);
    
    await cas.sleep(3000);
    await cas.type(page, "input[name=passwd]", process.env.AZURE_AD_USER_PASSWORD, true);
    await cas.pressEnter(page);
    await cas.sleep(4000);
    await cas.screenshot(page);
    await cas.click(page, "#idBtn_Back");
    await cas.sleep(7000);

    await cas.logPage(page);
    const result = new URL(page.url());
    await cas.log(result.searchParams.toString());
    assert(result.searchParams.has("ticket") === true);
    const ticket = result.searchParams.get("ticket");
    const json = await cas.validateTicket(service, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.name[0] === "CAS Test");
    assert(authenticationSuccess.attributes.preferred_username[0] === username);

    await browser.close();
})();
