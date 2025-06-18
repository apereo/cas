
const cas = require("../../cas.js");
const assert = require("assert");

async function loginWithToken(page, service, token) {
    await cas.log(`Logging in with SSO token to service ${service}`);
    await cas.gotoLogout(page);
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}&token=${token}`);
    await cas.sleep(1000);
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.logg("Login is successful");
}

(async () => {
    const service = "https://localhost:9859/anything/cas";
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogout(page);

    await cas.log("Generating SSO token");
    const response = await cas.doRequest(`https://localhost:8443/cas/actuator/tokenAuth/casuser?service=${service}`,
        "POST", {
            "Content-Type": "application/json",
            "Accept": "application/json"
        });
    let body = JSON.parse(response);
    console.dir(body, {depth: null, colors: true});
    assert(body.registeredService.id === 1);
    await loginWithToken(page, service, body.token);

    await cas.log("Checking for SSO token in service validation response");
    await cas.gotoLogout(page);
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    const ticket = await cas.assertTicketParameter(page);
    body = await cas.validateTicket(service, ticket, "XML");
    const token = body.match(/<cas:token>(.+)<\/cas:token>/)[1];
    await cas.log(`SSO Token ${token}`);
    await loginWithToken(page, service, token);
    
    await browser.close();
})();
