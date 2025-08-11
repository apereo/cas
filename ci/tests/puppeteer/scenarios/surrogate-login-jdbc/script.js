const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await impersonate(page, "casuser");
    
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page, "casimpersonated2+casuser", "Mellon");
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogout(page);

    await cas.closeBrowser(browser);
})();

async function impersonate(page, username) {
    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.loginWith(page, `+${username}`, "Mellon");
    await cas.sleep(1000);
    await cas.assertTextContent(page, "#titlePanel h2", "Choose Account");
    await cas.assertTextContentStartsWith(page, "#surrogateInfo", "You are provided with a list of accounts");
    await cas.assertVisibility(page, "#surrogateTarget");
    await cas.gotoLogout(page);
}
