const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Trying without an exising SSO session...");
    await cas.goto(page, "https://localhost:9876/fediz");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    await page.waitForSelector('#logincas', {visible: true});
    await cas.click(page, "#logincas");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    await page.waitForSelector('#username', {visible: true});
    await cas.loginWith(page);
    await page.waitForResponse(response => response.status() === 200);
    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    await cas.log(`Page URL: ${page.url()}`);
    await cas.assertInnerText(page, "#principalId", "casuser");
    await cas.assertVisibility(page, "#assertion");
    await page.waitForTimeout(2000);
    await cas.assertInnerText(page, "#claim0", "http://schemas.xmlsoap.org/claims/EmailAddress:casuser@example.org");
    await cas.assertInnerText(page, "#claim1", "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname:casuser");
    await cas.assertInnerText(page, "#claim2", "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress:CAS@example.org");

    await cas.log("Trying with an exising SSO session...");
    await cas.goto(page, "https://localhost:8443/cas/logout");
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await cas.goto(page, "https://localhost:9876/fediz");
    await page.waitForTimeout(2000);
    await page.waitForSelector('#logincas', {visible: true});
    await cas.click(page, "#logincas");
    await page.waitForTimeout(2000);
    await cas.log(`Page URL: ${page.url()}`);
    await cas.assertInnerText(page, "#principalId", "casuser");
    await cas.assertVisibility(page, "#assertion");
    await page.waitForTimeout(2000);
    await cas.assertInnerText(page, "#claim0", "http://schemas.xmlsoap.org/claims/EmailAddress:casuser@example.org");
    await cas.assertInnerText(page, "#claim1", "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname:casuser");
    await cas.assertInnerText(page, "#claim2", "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress:CAS@example.org");

    await browser.close();
    await process.exit(0);
})();

