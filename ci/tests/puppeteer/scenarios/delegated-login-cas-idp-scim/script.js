const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    const url = "https://localhost:8443/cas/oidc/oidcAuthorize?" +
        "client_id=client&" +
        "redirect_uri=https%3A%2F%2Foidcdebugger.com%2Fdebug&" +
        "scope=openid%20email%20profile%20address%20phone&" +
        "response_type=code&" +
        "response_mode=form_post&" +
        "nonce=vn4qulthnx";
    await cas.goto(page, url);

    await cas.assertVisibility(page, 'li #CasClient');
    await cas.click(page, "li #CasClient");
    await page.waitForNavigation();

    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    await cas.loginWith(page);
    await page.waitForTimeout(1000);

    let result = new URL(page.url());
    await cas.log(result.searchParams.toString());

    assert(result.searchParams.has("ticket") === false);
    assert(result.searchParams.has("client_id"));
    assert(result.searchParams.has("redirect_uri"));
    assert(result.searchParams.has("scope"));

    await cas.log("Allowing release of scopes and claims...");
    await cas.click(page, "#allow");
    await page.waitForNavigation();
    await page.waitForTimeout(2000);
    await cas.assertTextContent(page, "h1.green-text", "Success!");

    await cas.log(`Page URL: ${page.url()}`);
    await cas.screenshot(page);
    assert(await page.url().startsWith("https://oidcdebugger.com/debug"));

    await cas.doGet("http://localhost:9666/scim/v2/Users?attributes=userName",
        res => {
            assert(res.status === 200);
            let length = res.data.Resources.length;
            cas.log(`Found ${length} record`);
            assert(length === 1);
            assert(res.data.Resources[0].userName === "casuser")
        },
        error => {
            throw error;
        }, { 'Authorization': "Basic c2NpbS11c2VyOmNoYW5nZWl0" });

    await browser.close();
})();
