const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch();
    const page = await browser.newPage();
    await page.goto("http://localhost:8080/cas");
    await page.screenshot({ path: 'login.png' });
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    const title = await page.title();
    assert(title === "Log In Successful - CAS – Central Authentication Service")
    await browser.close();
})();