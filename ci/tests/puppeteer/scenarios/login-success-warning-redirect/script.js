const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/login?service=https://httpbin.org/anything/1");
    await page.waitForTimeout(1000);
    await cas.click(page, "#warn");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertTicketParameter(page);

    await cas.goto(page, "https://localhost:8443/cas/login?service=https://httpbin.org/anything/2");
    await cas.assertCookie(page, true, "CASPRIVACY");
    await cas.assertVisibility(page, "#ignorewarn");
    await page.waitForTimeout(1000);
    await cas.submitForm(page, "#fm1");
    await cas.assertTicketParameter(page);

    await cas.goto(page, "https://localhost:8443/cas/login?service=https://httpbin.org/anything/2");
    await cas.assertCookie(page, true, "CASPRIVACY");
    await page.waitForTimeout(1000);
    await cas.assertVisibility(page, "#ignorewarn");
    await cas.click(page, "#ignorewarn");
    await page.waitForTimeout(2000);
    await cas.submitForm(page, "#fm1");
    await cas.assertTicketParameter(page);

    await cas.goto(page, "https://localhost:8443/cas/login?service=https://httpbin.org/anything/3");
    await page.waitForTimeout(1000);
    await cas.assertInvisibility(page, "#ignorewarn");
    await cas.assertTicketParameter(page);

    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.assertCookie(page, false, "CASPRIVACY");
    
    await browser.close();
})();
