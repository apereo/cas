const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(3000);

    await cas.assertVisibility(page, '#loginProviders')
    await cas.assertVisibility(page, 'li #TwitterClient')
    await cas.assertVisibility(page, 'li #GitHubClient')

    await page.goto("https://localhost:8443/cas/clientredirect?client_name=GithubClient");
    await page.waitForTimeout(3000);

    await cas.assertVisibility(page, '#login_field')

    await browser.close();
})();
