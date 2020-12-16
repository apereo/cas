const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login?service=https://example.org");

    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    
    var element = await page.$('#content h2');
    var header = await page.evaluate(element => element.textContent, element);
    console.log(header)
    assert(header === "Attribute Consent")

    element = await page.$('#appTitle');
    header = await page.evaluate(element => element.textContent, element);
    console.log(header)
    assert(header === "The following attributes will be released to [https://example.org]:")

    element = await page.$('#first-name');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "first-name")

    element = await page.$('#first-name-value');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "[Apereo]")

    element = await page.$('#last-name');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "last-name")

    element = await page.$('#last-name-value');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "[CAS]")

    element = await page.$('#email');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "email")

    element = await page.$('#email-value');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "[casuser@example.org]")

    await click(page,"#optionsButton");
    await page.waitForTimeout(1000)

    var opt = await page.$('#optionAlways');
    assert(await opt != null);
    opt = await page.$('#optionAttributeName');
    assert(await opt != null);
    opt = await page.$('#optionAttributeValue');
    assert(await opt != null);
    
    element = await page.$('#reminderTitle');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "How often should I be reminded to consent again?")

    opt = await page.$('#reminder');
    assert(await opt != null);

    opt = await page.$('#reminderTimeUnit');
    assert(await opt != null);

    opt = await page.$('#confirm');
    assert(await opt != null);

    opt = await page.$('#cancel');
    assert(await opt != null);

    await browser.close();
})();

async function click(page, button) {
    await page.evaluate((button) => {
        document.querySelector(button).click();
    }, button);
}
