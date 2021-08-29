const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.setExtraHTTPHeaders({
        'Authorization': 'Negotiate unknown-token'
    })
    await page.goto("https://localhost:8443/cas/login");
    
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(3000)

    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    await browser.close();
})();
