const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://example.com";

    await page.goto(`https://localhost:8443/cas/login?service=${service}`);
    await cas.loginWith(page, "casuser", "Mellon");
    let ticket = await cas.assertTicketParameter(page);
    const body = await cas.doRequest(`https://localhost:8443/cas/serviceValidate?service=${service}&ticket=${ticket}`);
    console.log(body)
    assert(body.includes('<cas:serviceResponse xmlns:cas=\'http://www.yale.edu/tp/cas\'>'))
    assert(body.includes('<cas:user>casuser</cas:user>'))
    assert(body.includes('<cas:credentialType>UsernamePasswordCredential</cas:credentialType>'))
    assert(body.includes('<cas:isFromNewLogin>true</cas:isFromNewLogin>'))
    assert(body.includes('<cas:authenticationMethod>STATIC</cas:authenticationMethod>'))
    assert(body.includes('<cas:successfulAuthenticationHandlers>STATIC</cas:successfulAuthenticationHandlers>'))
    assert(body.includes('<cas:longTermAuthenticationRequestTokenUsed>false</cas:longTermAuthenticationRequestTokenUsed>'))
    await browser.close();
})();
