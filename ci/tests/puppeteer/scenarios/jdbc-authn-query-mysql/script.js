
const cas = require("../../cas.js");
const assert = require("assert");

const service = "https://localhost:9859/anything/cas";

async function loginAndValidate(page) {
    await cas.log("Attempting to login");
    await cas.gotoLogin(page, service);
    await cas.loginWith(page, "casuser", "TheBestPasswordEver");
    const ticket = await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.sleep(1000);
    const json = await cas.validateTicket(service, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes["first-name"] !== undefined);
    assert(authenticationSuccess.attributes["last-name"] !== undefined);
    assert(authenticationSuccess.attributes["phonenumber"] !== undefined);
    await cas.gotoLogout(page);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await loginAndValidate(page);

    await cas.log("Restarting MySQL server");
    const mysql = await cas.dockerContainer("mysql-server");
    await mysql.restart();
    await cas.sleep(2000);
    await loginAndValidate(page);
    await cas.closeBrowser(browser);
})();
