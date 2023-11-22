const puppeteer = require('puppeteer');
const assert = require('assert');

const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page);

    let pswd = await page.$('#password');
    assert(pswd == null);

    await cas.type(page,'#username', "casuser");
    await cas.pressEnter(page);
    await page.waitForNavigation();

    await page.waitForTimeout(2000);

    await cas.assertInvisibility(page, '#username');
    await cas.assertVisibility(page, '#password');

    await cas.type(page,'#password', "Mellon");
    await cas.pressEnter(page);
    await page.waitForNavigation();

    await page.waitForTimeout(2000);

    await cas.assertCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, casuser, have successfully logged in");

    await cas.click(page, "#auth-tab");
    await page.waitForTimeout(1000);
    await cas.type(page, "#attribute-tab-1 input[type=search]", "surrogate");
    await page.waitForTimeout(1000);
    await cas.screenshot(page);
    
    let surrogateEnabled = await page.$('#surrogateEnabled');
    assert(surrogateEnabled == null);
    let surrogatePrincipal = await page.$('#surrogatePrincipal');
    assert(surrogatePrincipal == null);
    let surrogateUser = await page.$('#surrogateUser');
    assert(surrogateUser == null);
    await page.waitForTimeout(1000);

    await browser.close();
})();
