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

    await browser.close();
})();
