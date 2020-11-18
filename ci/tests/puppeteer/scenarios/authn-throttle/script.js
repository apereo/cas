const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await submitLoginFailure(page);

    var header = await page.$eval('#content div.banner p', el => el.innerText)
    console.log(header)
    assert(header.startsWith("Authentication attempt has failed"))
    
    await submitLoginFailure(page);
    
    header = await page.$eval('#content h2', el => el.innerText)
    console.log(header)
    assert(header === "Access Denied")

    header = await page.$eval('#content p', el => el.innerText)
    console.log(header)

    await browser.close();
})();

    async function submitLoginFailure(page) {
        await page.goto("https://localhost:8443/cas/login");
        await page.type('#username', "casuser");
        await page.type('#password', "BadPassword1");
        await page.keyboard.press('Enter');
        await page.waitForNavigation();
    }


