const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    const context = browser.defaultBrowserContext()
    await context.overridePermissions("https://localhost:8443/cas/login", ['geolocation'])
    await page.setGeolocation({latitude:90, longitude:20})
    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)

    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(1000)

    let element = await page.$('#content h2');
    let header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "Authentication attempt is blocked.")

    element = await page.$('#content p');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "Your authentication attempt is untrusted and unauthorized from your current workstation.")
    
    await browser.close();
})();
