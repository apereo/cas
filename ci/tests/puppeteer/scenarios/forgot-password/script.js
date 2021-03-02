const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    await page.waitForTimeout(2000)

    var element = await page.$('#forgotPasswordLink');
    const link = await page.evaluate(element => element.textContent, element);
    console.log(link)
    assert(link === "Reset your password")

    await click(page, "#forgotPasswordLink")
    await page.waitForTimeout(1000)

    element = await page.$('#reset #fm1 h3');
    var header = await page.evaluate(element => element.textContent, element);
    console.log(header)
    assert(header === "Reset your password")
    
    let uid = await page.$('#username');
    assert(await uid.boundingBox() != null);

    await page.type('#username', "casuser");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await page.waitForTimeout(1000)

    element = await page.$('#content h2');
    header = await page.evaluate(element => element.textContent, element);
    console.log(header)
    assert(header === "Password Reset Instructions Sent Successfully.")
    
    element = await page.$('#content p');
    header = await page.evaluate(element => element.textContent, element);
    console.log(header)
    assert(header.startsWith("You should shortly receive a message"))

    await browser.close();
})();

async function click(page, button) {
    await page.evaluate((button) => {
        document.querySelector(button).click();
    }, button);
}
