const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    await cas.assertTextContent(page, "#forgotUsernameLink", "Forgot your username?");
    await cas.click(page, "#forgotUsernameLink")
    await page.waitForTimeout(1000)

    await cas.assertTextContent(page, '#reset #fm1 h3', "Forgot your username?");
    await cas.assertVisibility(page, '#email')

    await cas.type(page,'#email', "casuser@example.org");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await cas.assertTextContent(page, 'div .banner-danger p', "reCAPTCHA validation failed.")

    await browser.close();
})();


