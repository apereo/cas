const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    const url = "https://localhost:8443/cas/oidc/authorize?" +
        "client_id=client&" +
        "redirect_uri=https%3A%2F%2Foidcdebugger.com%2Fdebug&" +
        "scope=openid%20email%20profile%20address%20phone&" +
        "response_type=code&" +
        "response_mode=form_post&" +
        "nonce=vn4qulthnx";
    await page.goto(url);

    await cas.assertVisibility(page, 'li #CasClient')
    await cas.click(page, "li #CasClient")
    await page.waitForNavigation();

    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)

    console.log(page.url());
    let result = new URL(page.url());
    assert(result.searchParams.has("ticket") === false);

    await browser.close();
})();
