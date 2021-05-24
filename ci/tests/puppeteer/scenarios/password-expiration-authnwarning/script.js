const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/login");

    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await page.waitForTimeout(1000)

    let element = await page.$('#content h1');
    let header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "Authentication Succeeded with Warnings")

    let button = await page.$('#changePassword');
    assert(await button.boundingBox() != null);

    await page.$eval('#changePasswordForm', form => form.submit());
    await page.waitForTimeout(1000)

    element = await page.$('#pwdmain h3');
    header = await page.evaluate(element => element.textContent.trim(), element);
    console.log(header)
    assert(header === "You must change your password.")

    await typePassword(page, "123456", "123456")
    await page.waitForTimeout(1000)
    button = await page.$('#password-policy-violation-msg');
    assert(await button.boundingBox() != null);

    await typePassword(page, "123456", "123")
    await page.waitForTimeout(1000)
    button = await page.$('#password-confirm-mismatch-msg');
    assert(await button.boundingBox() != null);

    await typePassword(page, "Testing1234", "Testing1234")
    await page.waitForTimeout(1000)
    button = await page.$('#password-strength-msg');
    assert(await button.boundingBox() != null);
    button = await page.$('#password-strength-notes');
    assert(await button.boundingBox() != null);

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

    await page.$eval('#form', form => form.submit());
    await page.waitForTimeout(1000)

    element = await page.$eval('#content div h2', el => el.innerText.trim())
    console.log(element)
    assert(element === "Log In Successful")

    let tgc = (await page.cookies()).filter(value => value.name === "TGC")
    assert(tgc.length !== 0);

    await page.waitForTimeout(1000)
    await browser.close();
})();


async function typePassword(page, pswd, confirm) {
    await page.$eval('#password', el => el.value = '');
    await page.type('#password', pswd);

    await page.$eval('#confirmedPassword', el => el.value = '');
    await page.type('#confirmedPassword', confirm);
}
