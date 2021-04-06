const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    await page.type('#username', "testuser");
    await page.type('#password', "testuser");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    
    // await page.waitForTimeout(2000)

    let element = await page.$('#content h1');
    let header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "Authentication Interrupt")

    await page.$eval('#fm1', form => form.submit());
    await page.waitForTimeout(1000)
    
    element = await page.$('#content h1');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "Authentication Succeeded with Warnings")
    
    await page.$eval('#form', form => form.submit());
    await page.waitForTimeout(1000)

    let tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);

    header = await page.$eval('#content div h2', el => el.innerText.trim())
    console.log(header)
    assert(header === "Log In Successful")

    await browser.close();
})();
