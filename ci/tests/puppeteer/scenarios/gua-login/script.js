const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(2000)
    
    await page.type('#username', "casuser");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    
    // await page.waitForTimeout(2000)
    
    let element = await page.$('#login h2');
    let header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "casuser")

    element = await page.$('#guaInfo');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "If you do not recognize this image as yours, do NOT continue.")

    let guaImage = await page.$('#guaImage');
    assert(await guaImage.boundingBox() != null);

    await page.$eval('#fm1', form => form.submit());
    await page.waitForTimeout(1000)

    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    const tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);
    
    await browser.close();
})();
