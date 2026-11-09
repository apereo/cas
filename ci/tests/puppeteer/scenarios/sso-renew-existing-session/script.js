
const cas = require("../../cas.js");

const SERVICE = "https://example.com";

async function gatewayLogin(page, renew = false) {
    let url = `https://localhost:8443/cas/login?service=${SERVICE}&gateway=true`;
    if (renew) {
        url += "&renew=true";
    }
    await cas.goto(page, url);
    await cas.sleep(1000);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Gateway request without a single sign-on session returns to the service without a ticket");
    await page.deleteCookie({name: "TGC", url: "https://localhost:8443/cas/"});
    await gatewayLogin(page);
    await cas.assertPageUrlStartsWith(page, SERVICE);
    await cas.assertTicketParameter(page, false);

    await cas.gotoLogin(page, SERVICE);
    await cas.loginWith(page);
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);

    await cas.log("Gateway request with a single sign-on session issues a service ticket");
    await gatewayLogin(page);
    await cas.assertPageUrlStartsWith(page, SERVICE);
    await cas.assertTicketParameter(page);

    await cas.gotoLogin(page, "https://example.com&renew=true");
    await cas.sleep(1000);

    await cas.assertVisibility(page, "#existingSsoMsg");

    await cas.log("When both gateway and renew are requested, renew wins and the login form is shown");
    await gatewayLogin(page, true);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/login");
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#existingSsoMsg");

    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);

    await cas.closeBrowser(browser);
})();
