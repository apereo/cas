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
    await page.waitForTimeout(2000)

    const fileElement = await page.$("input[type=file]");
    let metadata = path.join(__dirname, '/saml-md/sp-metadata.xml');
    console.log("Metadata file: " + metadata);

    await fileElement.uploadFile(metadata);
    await click(page, "input[name='submit']")
    await page.waitForNavigation();
    await page.waitForTimeout(3000)

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(3000);
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await page.goto("https://localhost:8443/cas/login?service=https://github.com");
    await page.waitForTimeout(3000);

    let loginProviders = await page.$('#loginProviders');
    assert(await loginProviders.boundingBox() != null);

    let existingSsoMsg = await page.$('#existingSsoMsg');
    assert(await existingSsoMsg.boundingBox() != null);
    
    let client = await page.$('li #SAML2Client');
    assert(await client.boundingBox() != null);

    await click(page, "li #SAML2Client")
    await page.waitForNavigation();

    await page.waitForTimeout(2000)

    await page.type('#username', "morty");
    await page.type('#password', "panic");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(3000)

    await click(page, "input[name='_eventId_proceed']")
    await page.waitForTimeout(5000)

    const url = await page.url()
    console.log(`Page url: ${url}`)
    assert(url.startsWith("https://github.com/"))

    let result = new URL(page.url());
    let ticket = result.searchParams.get("ticket");
    console.log(ticket);
    assert(ticket != null);

    let metadataDir = path.join(__dirname, '/saml-md');
    fs.rmdirSync(metadataDir, { recursive: true });
    
    await browser.close();
})();

async function click(page, button) {
    await page.evaluate((button) => {
        document.querySelector(button).click();
    }, button);
}
