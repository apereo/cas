const puppeteer = require('puppeteer');
const assert = require('assert');
const fs = require('fs');
const path = require('path');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true,
        defaultViewport: null,
        args: ['--start-maximized']
    });
    const page = await browser.newPage();
    await page.goto("https://samltest.id/upload.php");
    // await page.waitForTimeout(1000)

    const fileElement = await page.$("input[type=file]");
    let metadata = path.join(__dirname, '/saml-md/idp-metadata.xml');
    console.log("Metadata file: " + metadata);

    await fileElement.uploadFile(metadata);
    // await page.waitForTimeout(1000)

    await click(page, "input[name='submit']")
    await page.waitForNavigation();

    // await page.waitForTimeout(1000)

    await page.goto("https://samltest.id/start-idp-test/");
    await page.type('input[name=\'entityID\']', "https://cas.apereo.org/saml/idp");
    // await page.waitForTimeout(1000)
    await click(page, "input[type='submit']")
    await page.waitForNavigation();

    // await page.waitForTimeout(1000)

    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(2000)

    let metadataDir = path.join(__dirname, '/saml-md');
    fs.rmdirSync(metadataDir, { recursive: true });

    let element = await page.$('div.entry-content p');
    const header = await page.evaluate(element => element.textContent, element);
    console.log(header)
    assert(header.startsWith("Your browser has completed the full SAML 2.0 round-trip"));
    
    await browser.close();
})();

async function click(page, button) {
    await page.evaluate((button) => {
        document.querySelector(button).click();
    }, button);
}
