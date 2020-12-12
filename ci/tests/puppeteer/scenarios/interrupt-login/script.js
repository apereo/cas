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

    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    
    // await page.waitForTimeout(20000)
    
    let element = await page.$('#content h1');
    let header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "Authentication Interrupt")

    element = await page.$('#content p');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header.startsWith("The authentication flow has been interrupted."));

    element = await page.$('#interruptMessage');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "We interrupted your login");

    let interruptLinks = await page.$('#interruptLinks');
    assert(await interruptLinks.boundingBox() != null);

    let attributesTable = await page.$('#attributesTable');
    assert(await attributesTable.boundingBox() != null);

    let field1 = await page.$('#field1');
    assert(await field1.boundingBox() != null);
    let field1Value = await page.$('#field1-value');
    assert(await field1Value.boundingBox() != null);

    let field2 = await page.$('#field2');
    assert(await field2.boundingBox() != null);
    let field2Value = await page.$('#field2-value');
    assert(await field2Value.boundingBox() != null);

    let cancel = await page.$('#cancel');
    assert(cancel == null);

    await page.$eval('#fm1', form => form.submit());
    await page.waitForTimeout(2000)

    const tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);

    await browser.close();
})();
