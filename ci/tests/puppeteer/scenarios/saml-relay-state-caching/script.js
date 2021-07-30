const puppeteer = require('puppeteer');
const assert = require('assert');
const path = require('path');
const fs = require('fs');

async function unsolicited(page, target) {
    let entityId = "https://samltest.id/saml/sp";
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    url += "&target=" + target;

    console.log("Navigating to " + url);
    await page.goto(url);
    await page.waitForNavigation();

    let result = await page.url()
    console.log(`Page url: ${result}`)
    assert(result.includes(target));
}

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://samltest.id/upload.php");
    await page.waitForTimeout(1000)

    const fileElement = await page.$("input[type=file]");
    let metadata = path.join(__dirname, '/saml-md/idp-metadata.xml');
    console.log("Metadata file: " + metadata);
    await page.waitForTimeout(1000)

    await fileElement.uploadFile(metadata);
    await click(page, "input[name='submit']")
    await page.waitForNavigation();
    await page.waitForTimeout(1000)

    await page.goto("https://localhost:8443/cas/login");
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(1000)

    await unsolicited(page, "https://first.com");
    await page.waitForTimeout(1000)
    await unsolicited(page, "https://example.com");
    await page.waitForTimeout(1000)

    let metadataDir = path.join(__dirname, '/saml-md');
    fs.rmdirSync(metadataDir, { recursive: true });
    
    await browser.close();
})();

async function click(page, button) {
    await page.evaluate((button) => {
        document.querySelector(button).click();
    }, button);
}
