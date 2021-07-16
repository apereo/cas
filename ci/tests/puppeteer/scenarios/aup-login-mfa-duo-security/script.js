const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://apereo.github.io");

    await cas.loginWith(page, "duobypass", "Mellon");
    await page.waitForTimeout(8000)
    
    const header = await cas.textContent(page, "#main-content #login #fm1 h3");
    assert(header === "Acceptable Usage Policy")

    await cas.assertVisibility(page, 'button[name=submit]')
    await cas.assertVisibility(page, 'button[name=cancel]')

    await cas.click(page, "#aupSubmit")
    await page.waitForNavigation();
    await page.waitForTimeout(2000)

    let result = new URL(page.url());
    assert(result.host === "apereo.github.io");
    let ticket = result.searchParams.get("ticket");
    console.log(ticket);
    assert(ticket != null);

    await page.goto("https://localhost:8443/cas/login")
    await page.waitForTimeout(2000)
    await cas.assertTicketGrantingCookie(page);
    
    await browser.close();
})();
