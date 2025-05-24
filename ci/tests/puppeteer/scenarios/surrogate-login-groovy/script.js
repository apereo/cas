
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await impersonate(page, "casuser1");
    await impersonate(page, "casuser2");

    await cas.log("Checking empty list of authorized accounts for user...");

    const service = "https://localhost:9859/anything/cas";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page, "+casuser3", "Mellon");
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogout(page);

    await cas.log("Checking auto selection for unauthorized user...");
    await cas.gotoLogin(page, service);
    await cas.loginWith(page, "anotheruser+casuser3", "Mellon");
    await cas.sleep(1000);
    await cas.assertInnerTextStartsWith(page, "#loginErrorsPanel p", "You are not authorized to impersonate");

    await cas.log("Checking wildcard access for authorized user...");
    await cas.gotoLogin(page, service);
    await cas.loginWith(page, "+casuser4", "Mellon");
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#content p", "you may directly proceed to log in");
    await cas.assertInnerTextContains(page, "#content p", "impersonation account selection is not allowed");

    await cas.gotoLogin(page, service);
    await cas.loginWith(page, "anybody+casuser4", "Mellon");
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogout(page);

    await browser.close();
})();

async function impersonate(page, username) {
    await cas.gotoLogin(page, "https://apereo.github.io");
    await cas.loginWith(page, `+${username}`, "Mellon");
    await cas.sleep(1000);
    await cas.assertTextContent(page, "#titlePanel h2", "Choose Account");
    await cas.assertTextContentStartsWith(page, "#surrogateInfo", "You are provided with a list of accounts");
    await cas.assertVisibility(page, "#surrogateTarget");
    await cas.gotoLogout(page);
}
