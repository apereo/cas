const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?service=https://github.com");

    await cas.loginWith(page, "casuser", "Mellon");

    await page.goto("https://localhost:8443/cas/login");
    // await page.waitForTimeout(1000)
    
    let tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);

    await page.goto("https://localhost:8443/cas/logout");
    // await page.waitForTimeout(5000)

    const header = await cas.innerText(page, '#content h2');

    assert(header === "Do you, casuser, want to log out completely?")

    await cas.assertVisibility(page, '#logoutButton')

    await cas.assertVisibility(page, '#divServices')

    await cas.assertVisibility(page, '#servicesTable')

    await cas.innerText(page, '#fm1');
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
