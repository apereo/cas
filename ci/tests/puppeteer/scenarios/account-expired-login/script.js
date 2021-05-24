const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    await page.type('#username', "expired");
    await page.type('#password', "expired");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    
    const header = await page.$eval('#pwdmain h3', el => el.innerText)
    console.log(header)
    assert(header === "Your password has expired.")

    let pwddesc = await page.$eval('#pwddesc', el => el.innerText)
    console.log(pwddesc)
    assert(pwddesc === "Please change your password.")
    
    await browser.close();
})();
