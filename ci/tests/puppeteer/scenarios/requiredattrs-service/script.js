const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();

    await page.goto("https://localhost:8443/cas/login?service=https://cn.admin.example.com");
    await submitLogin(page);
    await assertFailure(page);

    await page.goto("https://localhost:8443/cas/login?service=https://credtype.userpswd.example.com");
    await submitLogin(page);
    await assertFailure(page);

    await browser.close();
})();

async function submitLogin(page) {
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
}

async function assertFailure(page) {
    let header = await page.$eval('#loginErrorsPanel p', el => el.innerText)
    console.log(header)
    assert(header === "Service access denied due to missing privileges.")
}
