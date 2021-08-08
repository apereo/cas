const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(1000)
    
    await cas.assertVisibility(page, '#loginProviders')
    await cas.assertVisibility(page, 'li #TwitterClient')
    await cas.assertVisibility(page, 'li #CasClient')
    await cas.assertVisibility(page, 'li #GitHubClient')

    await page.goto("https://localhost:8443/cas/login?error=Fail&error_description=Error&error_code=400&error_reason=Reason");
    await page.waitForTimeout(1000);

    let header = await cas.textContent(page, "#content div h2");
    assert(header === "Unauthorized Access")
    await cas.assertInnerTextStartsWith(page, "div.entry-content p", "Your browser has completed the full SAML 2.0 round-trip");

    await cas.assertVisibility(page, '#errorTable')
    await cas.assertVisibility(page, '#loginLink')
    await cas.assertVisibility(page, '#appLink')

    await browser.close();
})();
