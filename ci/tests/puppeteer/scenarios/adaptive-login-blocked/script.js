const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const context = browser.defaultBrowserContext()
    await context.overridePermissions("https://localhost:8443/cas/login", ['geolocation'])
    await page.setGeolocation({latitude: 90, longitude: 20})
    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)

    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)
    await cas.assertTextContent(page, "#content h2", "Authentication attempt is blocked.");
    await cas.assertTextContent(page, "#content p", "Your authentication attempt is untrusted and unauthorized from your current workstation.");

    await browser.close();
})();
