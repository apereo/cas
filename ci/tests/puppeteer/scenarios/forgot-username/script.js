const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    // await page.waitForTimeout(1000)

    var element = await page.$('#forgotUsernameLink');
    const link = await page.evaluate(element => element.textContent, element);
    console.log(link)
    assert(link === "Forgot your username?")

    await click(page, "#forgotUsernameLink")
    
    // await page.click('#forgotUsernameLink');
    // await page.waitForNavigation();
    
    await page.waitForTimeout(1000)

    element = await page.$('#reset #fm1 h3');
    var header = await page.evaluate(element => element.textContent, element);
    console.log(header)
    assert(header === "Forgot your username?")

    let uid = await page.$('#email');
    assert(await uid.boundingBox() != null);

    await page.type('#email', "casuser@example.org");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    // await page.waitForTimeout(3000)

    element = await page.$('#content h2');
    header = await page.evaluate(element => element.textContent, element);
    console.log(header)
    assert(header === "Instructions Sent Successfully.")

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
