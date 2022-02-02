const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const authzUrl = "https://localhost:8443/cas/oidc/oidcAuthorize";
    const params = "client_id=client&" +
        "redirect_uri=https%3A%2F%2Foidcdebugger.com%2Fdebug&" +
        "scope=openid%20email%20profile%20address%20phone&" +
        "response_type=code&" +
        "response_mode=form_post&" +
        "nonce=vn4qulthnx";
    
    let url = `${authzUrl}?${params}`;
    let response = await page.goto(url);
    await page.waitForTimeout(1000)
    console.log(`Status: ${response.status()} ${response.statusText()}`)
    assert(403 === response.status())

    let value = `client:secret`;
    let buff = Buffer.alloc(value.length, value);
    let authzHeader = `Basic ${buff.toString('base64')}`;
    console.log(`Authorization header: ${authzHeader}`);

    const body = await cas.doRequest(`https://localhost:8443/cas/oidc/oidcPushAuthorize?${params}`, 'POST', {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Authorization': authzHeader
    }, 201);
    let result = JSON.parse(body)
    console.log(result);
    let requestUri = result.request_uri;
    assert(requestUri !== null);

    url = `${authzUrl}?client_id=client&request_uri=${requestUri}`;
    console.log(`Going to ${url}`)
    response = await page.goto(url);
    await page.waitForTimeout(1000)
    console.log(`Status: ${response.status()} ${response.statusText()}`)

    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)

    await cas.click(page, "#allow");
    await page.waitForNavigation();
    await page.waitForTimeout(1000)
    await cas.assertTextContent(page, "h1.green-text", "Success!");

    console.log(`Attempting to use request_uri ${requestUri}`)
    url = `${authzUrl}?client_id=client&request_uri=${requestUri}`;
    console.log(`Going to ${url}`)
    response = await page.goto(url);
    await page.waitForTimeout(1000)
    console.log(`Status: ${response.status()} ${response.statusText()}`)
    assert(403 === response.status())
    
    await browser.close();
})();

