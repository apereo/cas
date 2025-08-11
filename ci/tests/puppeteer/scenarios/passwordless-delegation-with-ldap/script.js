const cas = require("../../cas.js");

const service = "https://localhost:9859/anything/cas";

async function verifyDelegatedAuthenticationFlow(page) {
    await cas.gotoLogin(page, service);

    await cas.type(page, "#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);

    await cas.loginWith(page, "user1", "password");
    await cas.sleep(2000);

    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
}

async function verifyPasswordRequestFlow(page) {
    await cas.gotoLogin(page, service);

    await cas.type(page, "#username", "caspassword");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.sleep(1000);
    await cas.type(page, "#password", "p@$$word");
    await cas.pressEnter(page);
    await cas.sleep(2000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.gotoLogout(page);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await verifyDelegatedAuthenticationFlow(page);
    await verifyPasswordRequestFlow(page);

    await cas.closeBrowser(browser);
})();
