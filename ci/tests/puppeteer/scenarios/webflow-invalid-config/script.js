const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    // await page.waitForTimeout(2000)
    await page.$eval('input[name=_eventId]', el => el.value = 'unknown');
    await cas.loginWith(page, "casuser", "Mellon")
    await cas.assertInnerText(page, "#content h2", "Invalid/Unknown Webflow Configuration")
    await cas.assertInnerTextStartsWith(page, "#content p", "You are seeing this error because")
    await cas.assertInnerTextStartsWith(page, "#exceptionMessage", "No transition found")
    await browser.close();
})();
