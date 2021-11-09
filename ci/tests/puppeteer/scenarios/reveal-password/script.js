const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await cas.type(page, "#username", "casuser");
    await cas.type(page, "#password", "Mellon");
  
    let pwd = await page.$('.pwd');
    let pwdType = await page.evaluate(pwd => pwd.type, pwd);
    console.log(`password input type is ${pwdType}`);
    assert(pwdType === "password");
  
    console.log('click button to reveal password');
    await page.click('.reveal-password');
    pwdType = await page.evaluate(pwd => pwd.type, pwd);
    console.log(`password input type is ${pwdType}`);
    assert(pwdType === "text");

    console.log('click button to unreveal password');
    await page.click('.reveal-password');
    pwdType = await page.evaluate(pwd => pwd.type, pwd);
    console.log(`password input type is ${pwdType}`);
    assert(pwdType === "password");
  
    await browser.close();
})();
