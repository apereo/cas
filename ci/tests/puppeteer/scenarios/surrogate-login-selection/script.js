const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true,
        devtools: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?service=https://example.org");

    await page.type('#username', "+casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    
    // await page.waitForTimeout(20000)
    
    let element = await page.$('#titlePanel h2');
    let header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "Choose Account")

    element = await page.$('#surrogateInfo');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header.startsWith("You are provided with a list of accounts"));

    let surrogateTarget = await page.$('#surrogateTarget');
    assert(await surrogateTarget.boundingBox() != null);

    let submit = await page.$('#submit');
    assert(await submit.boundingBox() != null);

    let cancel = await page.$('#cancel');
    assert(cancel == null);

    let login = await page.$('#login');
    assert(await login.boundingBox() != null);

    await browser.close();
})();
