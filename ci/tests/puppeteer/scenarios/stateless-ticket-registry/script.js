
const assert = require("assert");
const cas = require("../../cas.js");

async function verifyAuthenticationFlow(context, service) {
    const page = await cas.newPage(context);
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);

    await cas.click(page, "#rememberMeButton");
    await cas.loginWith(page);
    await cas.sleep(2000);
    const ticket = await cas.assertTicketParameter(page);

    await cas.logb("Checking ticket validation response multiple times...");
    let body = await cas.doRequest(`https://localhost:8443/cas/validate?service=${service}&ticket=${ticket}`);
    await cas.log(body);
    assert(body === "yes\ncasuser\n");

    body = await cas.doRequest(`https://localhost:8443/cas/serviceValidate?service=${service}&ticket=${ticket}`);
    await cas.log(body);
    assert(body.includes("<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>"));
    assert(body.includes("<cas:user>casuser</cas:user>"));
    assert(body.includes("<cas:firstName>Bob</cas:firstName>"));
    assert(body.includes("<cas:lastName>Johnson</cas:lastName>"));
    assert(body.includes("<cas:employeeNumber>123456</cas:employeeNumber>"));
    assert(body.includes("<cas:credentialType>RememberMeUsernamePasswordCredential</cas:credentialType>"));
    assert(body.includes("<cas:authenticationMethod>STATIC</cas:authenticationMethod>"));
    assert(body.includes("<cas:isFromNewLogin>true</cas:isFromNewLogin>"));
    assert(body.includes("<cas:successfulAuthenticationHandlers>STATIC</cas:successfulAuthenticationHandlers>"));
    assert(body.includes("<cas:longTermAuthenticationRequestTokenUsed>true</cas:longTermAuthenticationRequestTokenUsed>"));

    const json = await cas.validateTicket(service, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes.credentialType[0] === "RememberMeUsernamePasswordCredential");
    assert(authenticationSuccess.attributes.isFromNewLogin[0] === true);
    assert(authenticationSuccess.attributes.authenticationDate[0] !== undefined);
    assert(authenticationSuccess.attributes.authenticationMethod[0] === "STATIC");
    assert(authenticationSuccess.attributes.successfulAuthenticationHandlers[0] === "STATIC");
    assert(authenticationSuccess.attributes.longTermAuthenticationRequestTokenUsed[0] === true);
    assert(authenticationSuccess.attributes.firstName[0] === "Bob");
    assert(authenticationSuccess.attributes.lastName[0] === "Johnson");
    assert(authenticationSuccess.attributes.employeeNumber[0] === "123456");
}

async function verifyExistingSsoSession(context, service) {
    const page = await cas.newPage(context);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.sleep(1000);

    const localStorageData = await cas.readLocalStorage(page);
    const storageContext = JSON.parse(localStorageData["CAS"]).CasBrowserStorageContext;
    assert(storageContext !== undefined);
    
    await cas.log(`Logging into service ${service}`);
    await cas.gotoLogin(page, service);
    await cas.sleep(2000);
    await cas.log("Checking for page URL...");
    await cas.logPage(page);
    await cas.assertInvisibility(page, "#username");
    await cas.assertInvisibility(page, "#password");
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(service, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes.credentialType[0] === "RememberMeUsernamePasswordCredential");
    assert(authenticationSuccess.attributes.isFromNewLogin[0] === true);
    assert(authenticationSuccess.attributes.authenticationDate[0] !== undefined);
    assert(authenticationSuccess.attributes.authenticationMethod[0] === "STATIC");
    assert(authenticationSuccess.attributes.successfulAuthenticationHandlers[0] === "STATIC");
    assert(authenticationSuccess.attributes.longTermAuthenticationRequestTokenUsed[0] === false);
    assert(authenticationSuccess.attributes.firstName[0] === "Bob");
    assert(authenticationSuccess.attributes.lastName[0] === "Johnson");
    assert(authenticationSuccess.attributes.employeeNumber[0] === "123456");
}

(async () => {
    const service = "https://localhost:9859/anything/lYzxki90TXtrk/7FPzc3OzJ4nNnVm/dPtVNRWdSqa8/TAIempOPCBbMPdje/gPpvsadQMANXyCCY/page.jsp?key=value&param=hello";
    const browser = await cas.newBrowser(cas.browserOptions());

    for (let i = 1; i <= 2; i++) {
        const context = await browser.createBrowserContext();
        await cas.log(`Running test scenario ${i}`);
        switch (i) {
        case 1:
            await verifyAuthenticationFlow(context, service);
            break;
        case 2:
            await verifyExistingSsoSession(context, service);
            break;
        }
        await context.close();
        await cas.log("=======================================");
    }

    await browser.close();

})();
