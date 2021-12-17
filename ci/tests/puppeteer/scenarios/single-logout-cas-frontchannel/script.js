const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8444");
    await page.waitForTimeout(1000)
    await page.goto("https://localhost:8444/protected");
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)
    let url = await page.url()
    console.log(`Page url: ${url}`)
    assert(url.startsWith("https://localhost:8444/protected"));
    await cas.assertInnerTextContains(page, "div.starter-template h2 span", "casuser")
    await page.goto("https://localhost:8443/cas/logout");
    await page.waitForTimeout(2000)

    await cas.assertInnerText(page, "div h2", "Logout successful")
    await cas.assertInnerTextStartsWith(page, "div p", "You have successfully logged out")
    await cas.assertInnerTextStartsWith(page, "div ul li p kbd", "https://localhost:8444/protected")

    await page.goto("https://localhost:8444/protected");
    await page.waitForTimeout(2000)
    url = await page.url()
    console.log(`Page url: ${url}`)
    assert(url.startsWith("https://localhost:8443/cas/login?service="));
    await cas.killProcess("java", ".*bootiful-cas-client.*");
    await browser.close();
})();
