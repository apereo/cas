const puppeteer = require('puppeteer');
const assert = require('assert');
const path = require('path');
const fs = require('fs');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
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

    let entityId = "https://samltest.id/saml/sp";
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";
    url += "&time=60"

    console.log("Navigating to " + url);
    await page.goto(url);
    await page.waitForTimeout(1000)

    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(2000);

    const title = await page.title();
    console.log(title);
    assert(title === "CAS - Central Authentication Service")

    let element = await page.$('#content div h2');
    let header = await page.evaluate(element => element.textContent.trim(), element);
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
