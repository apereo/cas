const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await page.waitForTimeout(2000);
    await cas.assertCookie(page, false);
    await cas.screenshot(page);
    await cas.assertInnerText(page, "#content h2", "Weak Password Detected");
    await cas.click(page, "#submit");
    await page.waitForNavigation();

    await cas.assertInnerText(page, "#pwdmain h3", "Hello, casuser. You must change your password.");
    await cas.type(page,'#password', "P@ssw0rd9");
    await cas.type(page,'#confirmedPassword', "P@ssw0rd9");
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await cas.assertInnerText(page, "#content h2", "Password Change Successful");
    
    await browser.close();
})();
