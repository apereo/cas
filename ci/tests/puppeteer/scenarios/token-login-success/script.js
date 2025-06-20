
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
    let token = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMTkyQ0JDLUhTMzg0IiwiYWxnIjoiZGlyIn0..zTe4qLwuyvVi6gHmSL4VcQ.NAmUxtxfEc-xJ0BO1DIUtxRN05-XTiB4bFVqN4YFvBPtVUrppTR5oXVKszWYQD_jWuhHnUBCVcvOBri9n-q6rKXQBkMnW9TLXQ4d9waJUOKpxORvgX3T56qoVfYTbUkVxe-5VchX000JWy8GdhpyWawldG0au03GhU7jhVnQeMlb7WWaNFOGXQwx6wvF0B30UL-6wgZO1nWD7InaQXJiFZazE0HK0DX61DUbP6PFYJKOBkbSWg9vSSzCeTxVFx9uJXMSAg9_vacpAYmq02ixV8e73CU9_hHhiCqEOYGunzwO4mEm6mn3fhPz6Q5azzPhZTc-lROKc66bmo_Y7jtMRmwgwBTq4diY9bIhw0x_qMsLLEdk1qk-dH9_FhBcunW2PkM8rCyGyDQ2slL-Axs_zg.HgP2g-UNsljcDhO74OMFWKxkYiSo4mbK";

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

    const response = await cas.doRequest(`https://localhost:8443/cas/actuator/tokenAuth/casuser?service=${service}`,
        "POST", {
            "Content-Type": "application/json",
            "Accept": "application/json"
        });
    body = JSON.parse(response);
    assert(body.registeredService.id === 1);
    await loginWithToken(page, service, body.token);
    await browser.close();
})();
