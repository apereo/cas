const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

async function getOneTimeCode(browser) {
    const page = await browser.newPage();
    await page.goto("http://localhost:8282");
    await page.waitForTimeout(1000);
    await cas.click(page, "table tbody td a");
    await page.waitForTimeout(3000);
    let code = await cas.textContent(page, "div[name=bodyPlainText] .well");
    await page.close();
    return code;
}

async function impersonatePreSelected(page, browser) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, "user3+casuser", "Mellon");
    await cas.assertVisibility(page, '#token');
    await page.waitForTimeout(1000);
    let code = await getOneTimeCode(browser);
    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.assertCookie(page);
    await page.waitForTimeout(1000);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged in");
    await page.waitForTimeout(2000);
    await cas.gotoLogout(page);
}

async function impersonateWithMenu(page, browser) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, "+casuser", "Mellon");
    await page.waitForTimeout(1000);
    await cas.assertVisibility(page, '#token');
    let code = await getOneTimeCode(browser);
    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(1000);
    await cas.assertTextContentStartsWith(page, "#surrogateInfo", "You are provided with a list of accounts");
    await page.select('#surrogateTarget', 'user3');
    await cas.click(page, "#submit");
    await page.waitForNavigation();
    await page.waitForTimeout(1000);
    await cas.assertCookie(page);
    await page.waitForTimeout(1000);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged in");
    await page.waitForTimeout(2000);
    await cas.gotoLogout(page);
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await impersonatePreSelected(page, browser);
    await impersonateWithMenu(page, browser);
    await browser.close();
})();
