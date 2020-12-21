const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    console.log("Attempting invalid password reset without SSO")
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?pswdrst=bad_token_with_no_sso");
    let header = await page.$eval('#content h2', el => el.innerText)
    console.log(header)
    assert(header === "Password Reset Failed")

    console.log("Attempting invalid password reset with SSO")
    await page.goto("https://localhost:8443/cas/login");
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.goto("https://localhost:8443/cas/login?pswdrst=bad_token_with_sso");
    header = await page.$eval('#content h2', el => el.innerText)
    console.log(header)
    assert(header === "Password Reset Failed")

    await browser.close();
})();
