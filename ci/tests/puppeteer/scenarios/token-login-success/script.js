
const cas = require("../../cas.js");
const assert = require("assert");

async function loginWithToken(page, service, token) {
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}&token=${token}`);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
}

(async () => {
    const service = "https://localhost:9859/anything/cas";
    let token = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTkyQ0JDLUhTMzg0IiwiY3R5IjoiSldUIn0..6q-WnspGZkzHAd_4LDbRnA.Crc5YCWpZ26j9iJ61RR0t7L_A1NtEbTe-IaqqBrEZBlLFxGIcchMGC6sPXOasewh3GwkukrHGyg5pfXU8OvPHWPvC8riG5T1rEYkfgxVoG2rUOIFrLYHGpLCRdsXRP3EYnE4nnIePlDTmKLVC80ovtUoonow2YVwMK6mv1mqWE-RN-XSv_gav4aroaNvY4AA19Q_ppBYc_VVUJmz1t0pr1BTIRao8Hm56LQU8_kSBDfEuB96wTPpuTafZ3zMshgkLM--Ga-FGLL2Tyx_toMdaQ.QtUB4sH8R0BQ0YsB8QmC-ccAtgwGhLZu";

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await loginWithToken(page, service, token);

    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    const ticket = await cas.assertTicketParameter(page);
    let body = await cas.validateTicket(service, ticket, "XML");
    token = body.match(/<cas:token>(.+)<\/cas:token>/)[1];
    await cas.log(`SSO Token ${token}`);
    await loginWithToken(page, service, token);

    const response = await cas.doRequest(`https://localhost:8443/cas/actuator/tokenAuth/${token}?service=${service}`,
        "GET", {
            "Content-Type": "application/json",
            "Accept": "application/json"
        });
    body = JSON.parse(response);
    assert(body.registeredService.id === 1);
    assert(body.registeredService.properties === undefined);
    assert(body.principal.id === "casuser");
    await cas.closeBrowser(browser);
})();
