const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    var element = await page.$('#webauthnLoginPanel div div h2#status');
    assert(await element.boundingBox() == null);
    
    await page.type('#username', "casuser");
    await page.keyboard.press('Tab');
    
    element = await page.$('#webauthnLoginPanel div div h2#status');
    assert(await element.boundingBox() != null);
    const header = await page.evaluate(element => element.textContent, element);
    console.log(header)
    assert(header === "Login with FIDO2-enabled Device");

    await browser.close();
})();
