const puppeteer = require('puppeteer');
const assert = require('assert');
const url = require('url');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    await cas.loginWith(page, "casuser", "Mellon");
    
    // await page.waitForTimeout(20000)
    
    let header = await cas.textContent(page, "#content h1");
    assert(header === "Authentication Interrupt")

    header = await cas.textContent(page, "#content p");
    assert(header.startsWith("The authentication flow has been interrupted"));

    header = await cas.textContent(page, "#interruptMessage");
    assert(header === "We interrupted your login");

    const tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);
    
    await cas.assertVisibility(page, '#interruptLinks')

    await cas.assertVisibility(page, '#attributesTable')

    await cas.assertVisibility(page, '#field1')
    await cas.assertVisibility(page, '#field1-value')

    await cas.assertVisibility(page, '#field2')
    await cas.assertVisibility(page, '#field2-value')

    let cancel = await page.$('#cancel');
    assert(cancel == null);

    await page.$eval('#fm1', form => form.submit());
    await page.waitForTimeout(2000)
    
    await browser.close();
})();
