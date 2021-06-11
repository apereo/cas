const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await page.waitForTimeout(2000)

    let link = await cas.textContent(page, "#forgotPasswordLink");
    assert(link === "Reset your password")

    await cas.click(page, "#forgotPasswordLink")
    await page.waitForTimeout(1000)

    let header = await cas.textContent(page, "#reset #fm1 h3");
    assert(header === "Reset your password")
    
    await cas.assertVisibility(page, '#username')

    await cas.type(page,'#username', "casuser");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await page.waitForTimeout(1000)

    header = await cas.textContent(page, "div .banner-danger p");
    assert(header === "reCAPTCHA validation failed.")

    await browser.close();
})();

