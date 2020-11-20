const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-gauth");
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    
    let element = await page.$('#login h4');
    const header = await page.evaluate(element => element.textContent, element);
    console.log(header)
    assert(header.startsWith("Your account is not registered"))

    let image = await page.$('#login table img');
    assert(await image.boundingBox() != null);

    let seckey = await page.$('#seckeypanel pre');
    assert(await seckey.boundingBox() != null);

    let scratchcodePanel = await page.$('#scratchcodes');
    assert(await scratchcodePanel.boundingBox() != null);
    assert(5 == (await page.$$('#scratchcodes div.mdc-chip')).length)

    let confirm = await page.$("#confirm");
    await confirm.click();
    let title = await page.$('#confirm-reg-dialog #notif-dialog-title');
    let titleText = await page.evaluate(title => title.textContent, title);
    console.log(titleText)
    assert(await title.boundingBox() != null);

    let token = await page.$('#token');
    assert(await token.boundingBox() != null);

    let accountName = await page.$('#accountName');
    assert(await accountName.boundingBox() != null);

    await browser.close();
})();
