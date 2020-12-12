const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    let tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);

    await page.goto("https://localhost:8443/cas/logout");
    // await page.waitForTimeout(1000)

    const header = await page.$eval('#content h2', el => el.innerText)
    console.log(header)
    assert(header === "Do you want to log out completely?")

    let button = await page.$('#logoutButton');
    assert(await button.boundingBox() != null);

    await page.$eval('#fm1', form => form.submit());
    // await page.waitForNavigation();
    await page.waitForTimeout(1000)

    const url = await page.url()
    console.log(`Page url: ${url}`)
    assert(url === "https://localhost:8443/cas/logout")

    // await page.waitForTimeout(20000)

    tgc = (await page.cookies()).filter(value => value.name === "TGC")
    console.log(tgc)
    assert(tgc.length === 0);

    await browser.close();
})();
