const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?authn_method=mfa-gauth");
    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(5000)

    let form1 = await page.$('#form-1');
    assert(await form1.boundingBox() != null);
    let name1 = await page.$('#name-RecordName');
    assert(await name1.boundingBox() != null);
    let id1 = await page.$('#id-1');
    assert(await id1.boundingBox() != null);

    let form2 = await page.$('#form-2');
    assert(await form2.boundingBox() != null);
    let name2 = await page.$('#name-RecordName2');
    assert(await name2.boundingBox() != null);
    let id2 = await page.$('#id-2');
    assert(await id2.boundingBox() != null);
    
    let register = await page.$('#register');
    assert(await register.boundingBox() != null);

    await page.$eval('#form-1', form => form.submit());
    await page.waitForTimeout(1000)

    let element = await page.$('#login p');
    const header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header.startsWith("Your selected device for multifactor authentication is"))

    let token = await page.$('#token');
    assert(await token.boundingBox() != null);

    let login = await page.$('#login');
    assert(await login.boundingBox() != null);

    let cancel = await page.$('#cancel');
    assert(await cancel.boundingBox() != null);

    register = await page.$('#register');
    assert(await register.boundingBox() != null);

    let selectDeviceButton = await page.$('#selectDeviceButton');
    assert(await selectDeviceButton.boundingBox() != null);

    await browser.close();
})();
