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

    let element = await page.$('#forgotPasswordLink');
    let link = await page.evaluate(element => element.textContent, element);
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

    assert("none" === await uid.evaluate(el => el.getAttribute("autocapitalize")))
    assert("false" === await uid.evaluate(el => el.getAttribute("spellcheck")))
    assert("username" === await uid.evaluate(el => el.getAttribute("autocomplete")))

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

    await page.goto("http://localhost:8282");
    await page.waitForTimeout(1000)
    await click(page, "table tbody td a")
    await page.waitForTimeout(1000)

    element = await page.$('div[name=bodyPlainText] .well');
    link = await page.evaluate(element => element.textContent, element);
    console.log(link)

    await page.goto(link);
    await page.waitForTimeout(1000)

    element = await page.$('#content h2');
    header = await page.evaluate(element => element.textContent, element);
    console.log(header)
    assert(header === "Answer Security Questions")

    await page.type('#q0', "answer1");
    await page.type('#q1', "answer2");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(1000)

    await typePassword(page, "EaP8R&iX$eK4nb8eAI", "EaP8R&iX$eK4nb8eAI")
    await page.waitForTimeout(1000)
    button = await page.$('#password-confirm-mismatch-msg');
    assert(await button.boundingBox() == null);
    button = await page.$('#password-policy-violation-msg');
    assert(await button.boundingBox() == null);

    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    element = await page.$('#content h2');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "Password Change Successful")

    element = await page.$('#content p');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "Your account password is successfully updated.")
    
    await browser.close();
})();

async function click(page, button) {
    await page.evaluate((button) => {
        document.querySelector(button).click();
    }, button);
}

async function typePassword(page, pswd, confirm) {
    await page.$eval('#password', el => el.value = '');
    await page.type('#password', pswd);

    await page.$eval('#confirmedPassword', el => el.value = '');
    await page.type('#confirmedPassword', confirm);
}
