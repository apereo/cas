const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    let url = "https://localhost:8443/cas/oidc/oidcAuthorize?" +
        "client_id=client&" +
        "redirect_uri=https%3A%2F%2Foidcdebugger.com%2Fdebug&" +
        "scope=openid%20email%20profile%20address%20phone&" +
        "response_type=code&" +
        "response_mode=form_post&" +
        "nonce=vn4qulthnx";
    console.log(`First attempt: navigating to ${url}`);
    await page.goto(url);

    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)

    await cas.click(page, "#allow");
    await page.waitForNavigation();
    await page.waitForTimeout(3000)

    let header = await cas.textContent(page, "h1.green-text");
    assert(header === "Success!")

    url = `${url}&prompt=login`;
    console.log(`Second attempt: navigating to ${url}`);
    await page.goto(url);
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");
    
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(2000)

    header = await cas.textContent(page, "h1.green-text");
    assert(header === "Success!")

    await browser.close();
})();

