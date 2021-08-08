const puppeteer = require('puppeteer');
const assert = require('assert');

const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await page.goto("https://localhost:8443/cas/login");

    let pswd = await page.$('#password');
    assert(pswd == null);

    let uid = await page.$('#username');
    await cas.assertAttribute(uid, "autocapitalize", "none");
    await cas.assertAttribute(uid, "spellcheck", "false");
    await cas.assertAttribute(uid, "autocomplete", "username");

    await cas.type(page,'#username', "casuser");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(10000)

    const header = await cas.innerText(page, '#login h3');

    assert(header === "Provide Token")

    const sub = await cas.innerText(page, '#login p');
    console.log(sub)
    assert(sub.startsWith("Please provide the security token sent to you"));

    await cas.assertVisibility(page, '#token')
    
    await browser.close();
})();
