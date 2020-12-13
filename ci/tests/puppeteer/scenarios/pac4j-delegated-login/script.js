const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(1000)
    
    var loginProviders = await page.$('#loginProviders');
    assert(await loginProviders.boundingBox() != null);

    var twitter = await page.$('li #TwitterClient');
    assert(await twitter.boundingBox() != null);

    var cas = await page.$('li #CasClient');
    assert(await cas.boundingBox() != null);

    var github = await page.$('li #GitHubClient');
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

    var errorTable = await page.$('#errorTable');
    assert(await errorTable.boundingBox() != null);

    var loginLink = await page.$('#loginLink');
    assert(await loginLink.boundingBox() != null);

    var appLink = await page.$('#appLink');
    assert(await appLink.boundingBox() != null);

    await browser.close();
})();
