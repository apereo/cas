const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(1000)
    
    let loginProviders = await page.$('#loginProviders');
    assert(await loginProviders.boundingBox() != null);

    let twitter = await page.$('li #TwitterClient');
    assert(await twitter.boundingBox() != null);

    let cas = await page.$('li #CasClient');
    assert(await cas.boundingBox() != null);

    let github = await page.$('li #GitHubClient');
    assert(await github.boundingBox() != null);

    await page.goto("https://localhost:8443/cas/login?error=Fail&error_description=Error&error_code=400&error_reason=Reason");
    await page.waitForTimeout(1000);

    let element = await page.$('#content div h2');
    let header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "Unauthorized Access")

    element = await page.$('#content div p');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header.startsWith("Either the authentication request was rejected/cancelled"));

    let errorTable = await page.$('#errorTable');
    assert(await errorTable.boundingBox() != null);

    let loginLink = await page.$('#loginLink');
    assert(await loginLink.boundingBox() != null);

    let appLink = await page.$('#appLink');
    assert(await appLink.boundingBox() != null);

    await browser.close();
})();
