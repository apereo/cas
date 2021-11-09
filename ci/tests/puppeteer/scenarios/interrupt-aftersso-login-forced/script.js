const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    console.log("Service has disabled interrupt, but will establish single sign-on session")
    await page.goto("https://localhost:8443/cas/login?service=https://httpbin.org/get?nointerrupt");
    await cas.loginWith(page, "casuser", "Mellon");
    await cas.assertTicketParameter(page);
    await page.goto("https://localhost:8443/cas/login");
    await cas.assertTicketGrantingCookie(page);

    console.log("Service has force-execution for interrupt; every attempt must force interrupt")
    for (let i = 1; i <= 3; i++) {
        await page.goto("https://localhost:8443/cas/login?service=https://httpbin.org/get?interrupt-forced");
        await page.waitForTimeout(1000)
        await cas.assertTextContent(page, "#content h1", "Authentication Interrupt")
        await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
        await cas.assertTextContentStartsWith(page, "#interruptMessage", "We interrupted your login");
        await cas.submitForm(page, "#fm1");
        await cas.assertTicketParameter(page);
        await page.goto("https://localhost:8443/cas/login");
        await cas.assertTicketGrantingCookie(page);
    }
    await browser.close();
})();
