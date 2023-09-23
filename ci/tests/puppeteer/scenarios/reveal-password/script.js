const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.type(page, "#username", "casuser");
    await cas.type(page, "#password", "Mellon");
  
    let pwd = await page.$('.pwd');
    let pwdType = await page.evaluate(pwd => pwd.type, pwd);
    await cas.log(`password input type is ${pwdType}`);
    assert(pwdType === "password");
  
    await cas.log('click button to reveal password');
    await page.click('.reveal-password');
    pwdType = await page.evaluate(pwd => pwd.type, pwd);
    await cas.log(`password input type is ${pwdType}`);
    assert(pwdType === "text");

    await cas.log('click button to unreveal password');
    await page.click('.reveal-password');
    pwdType = await page.evaluate(pwd => pwd.type, pwd);
    await cas.log(`password input type is ${pwdType}`);
    assert(pwdType === "password");
  
    await browser.close();
})();
