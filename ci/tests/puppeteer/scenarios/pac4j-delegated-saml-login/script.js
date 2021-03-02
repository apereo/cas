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

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)
    
    await page.goto("https://samltest.id/upload.php");
    await page.waitForTimeout(1000)

    const fileElement = await page.$("input[type=file]");
    let metadata = path.join(__dirname, '/saml-md/sp-metadata.xml');
    console.log("Metadata file: " + metadata);

    await fileElement.uploadFile(metadata);
    await click(page, "input[name='submit']")
    await page.waitForNavigation();
    await page.waitForTimeout(1000)

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000);

    var loginProviders = await page.$('#loginProviders');
    assert(await loginProviders.boundingBox() != null);

    var client = await page.$('li #SAML2Client');
    assert(await client.boundingBox() != null);
    
    await click(page, "li #SAML2Client")
    await page.waitForNavigation();

    await page.waitForTimeout(1000)

    await page.type('#username', "morty");
    await page.type('#password', "panic");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(1000)

    await click(page, "input[name='_eventId_proceed']")
    await page.waitForTimeout(1000)

    const tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);

    const title = await page.title();
    console.log(title)
    assert(title === "CAS - Central Authentication Service")

    const header = await page.$eval('#content div h2', el => el.innerText)
    console.log(header)
    assert(header === "Log In Successful")
    
    let metadataDir = path.join(__dirname, '/saml-md');
    fs.rmdirSync(metadataDir, { recursive: true });
    
    await browser.close();
})();

async function click(page, button) {
    await page.evaluate((button) => {
        document.querySelector(button).click();
    }, button);
}
