const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

async function getOneTimeCode(browser) {
    return cas.extractFromEmailMessage(browser);
}

async function impersonatePreSelected(page, browser) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, "user3+casuser", "Mellon");
    await cas.assertVisibility(page, "#token");
    await cas.waitForTimeout(page);
    const code = await getOneTimeCode(browser);
    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.assertCookie(page);
    await cas.waitForTimeout(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged in");
    await cas.waitForTimeout(page);
    await cas.gotoLogout(page);
}

async function impersonateWithMenu(page, browser) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, "+casuser", "Mellon");
    await cas.waitForTimeout(page);
    await cas.assertVisibility(page, "#token");
    const code = await getOneTimeCode(browser);
    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.waitForTimeout(page);
    await cas.assertTextContentStartsWith(page, "#surrogateInfo", "You are provided with a list of accounts");
    await page.select("#surrogateTarget", "user3");
    await cas.click(page, "#submit");
    await cas.waitForNavigation(page);
    await cas.waitForTimeout(page);
    await cas.assertCookie(page);
    await cas.waitForTimeout(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged in");
    await cas.waitForTimeout(page);
    await cas.gotoLogout(page);
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await impersonatePreSelected(page, browser);
    await impersonateWithMenu(page, browser);
    await browser.close();
})();
