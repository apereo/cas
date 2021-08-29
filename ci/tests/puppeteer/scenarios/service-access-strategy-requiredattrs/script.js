const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.goto("https://localhost:8443/cas/login?service=https://cn.admin.example.com");
    await submitLogin(page);
    await assertFailure(page);

    await page.goto("https://localhost:8443/cas/login?service=https://credtype.userpswd.example.com");
    await submitLogin(page);
    await assertFailure(page);

    await page.goto("https://localhost:8443/cas/login");
    await submitLogin(page);
    await page.goto("https://localhost:8443/cas/login?service=https://credtype.userpswd.example.com");
    await assertFailure(page);
    await browser.close();
})();

async function submitLogin(page) {
    await cas.loginWith(page, "casuser", "Mellon");
}

async function assertFailure(page) {
    await cas.assertInnerText(page, "#loginErrorsPanel p", "Service access denied due to missing privileges.")
    await page.waitForTimeout(1000)
}
