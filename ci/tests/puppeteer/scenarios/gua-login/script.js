const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    let uid = await page.$('#username');
    assert("none" === await uid.evaluate(el => el.getAttribute("autocapitalize")))
    assert("false" === await uid.evaluate(el => el.getAttribute("spellcheck")))
    assert("username" === await uid.evaluate(el => el.getAttribute("autocomplete")))
    
    // await page.waitForTimeout(2000)
    
    await page.type('#username', "casuser");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    
    // await page.waitForTimeout(2000)
    
    let header = await cas.textContent(page, "#login h2");

    assert(header === "casuser")

    header = await cas.textContent(page, "#guaInfo");

    assert(header === "If you do not recognize this image as yours, do NOT continue.")

    await cas.assertVisibility(page, '#guaImage')

    await cas.submitForm(page, "#fm1");

    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    const tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);
    
    await browser.close();
})();
