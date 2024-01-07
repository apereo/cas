const puppeteer = require("puppeteer");
const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const service = "https://localhost:9859/anything/lYzxki90TXtrk/7FPzc3OzJ4nNnVm/dPtVNRWdSqa8/TAIempOPCBbMPdje/gPpvsadQMANXyCCY/page.jsp?key=value&param=hello";
    await cas.gotoLogin(page, service);
    await cas.click(page, "#rememberMe");
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    const ticket = await cas.assertTicketParameter(page);
    await browser.close();

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

    body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    const json = JSON.parse(body);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes.credentialType[0] === "RememberMeUsernamePasswordCredential");
    assert(authenticationSuccess.attributes.isFromNewLogin[0] === true);
    assert(authenticationSuccess.attributes.authenticationDate[0] !== null);
    assert(authenticationSuccess.attributes.authenticationMethod[0] === "STATIC");
    assert(authenticationSuccess.attributes.successfulAuthenticationHandlers[0] === "STATIC");
    assert(authenticationSuccess.attributes.longTermAuthenticationRequestTokenUsed[0] === true);
    assert(authenticationSuccess.attributes.firstName[0] === "Bob");
    assert(authenticationSuccess.attributes.lastName[0] === "Johnson");
    assert(authenticationSuccess.attributes.employeeNumber[0] === "123456");

})();
