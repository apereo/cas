const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    await cas.loginWith(page, "testuser", "testuser");

    // await page.waitForTimeout(2000)

    let header = await cas.textContent(page, "#content h1");

    assert(header === "Authentication Interrupt")

    await page.$eval('#fm1', form => form.submit());
    await page.waitForTimeout(1000)
    
    header = await cas.textContent(page, "#content h1");

    assert(header === "Authentication Succeeded with Warnings")
    
    await page.$eval('#form', form => form.submit());
    await page.waitForTimeout(1000)

    let tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);

    header = await cas.innerText(page, '#content div h2');

    assert(header === "Log In Successful")

    await browser.close();
})();
