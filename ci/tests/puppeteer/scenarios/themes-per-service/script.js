const puppeteer = require('puppeteer');
const assert = require('assert');

const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://github.com/apereo/cas");
    await page.waitForTimeout(1000)

    await cas.assertVisibility(page, '#twitter-link')
    await cas.assertVisibility(page, '#youtube-link')

    const imgs = await page.$$eval('#cas-logo',
        imgs => imgs.map(img => img.getAttribute('src')));
    let logo = imgs.pop();
    console.log(logo)
    assert(logo === "/cas/themes/example/images/logo.png")

    console.log("Logging out...")
    await page.goto("https://localhost:8443/cas/logout?service=https://github.com/apereo/cas");
    await page.waitForTimeout(1000)

    await cas.assertVisibility(page, '#twitter-link')
    await cas.assertVisibility(page, '#youtube-link')
    
    await cas.assertVisibility(page, '#logoutButton')
    await cas.submitForm(page, "#fm1");

    await page.waitForTimeout(1000)
    const url = await page.url()
    console.log(`Page url: ${url}`)
    assert(url.toString().startsWith("https://localhost:8443/cas/logout"))
    await cas.assertNoTicketGrantingCookie(page);

    await cas.assertVisibility(page, '#twitter-link')
    await cas.assertVisibility(page, '#youtube-link')
    
    await browser.close();
})();
