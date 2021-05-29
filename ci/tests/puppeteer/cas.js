const assert = require('assert');

const BROWSER_OPTIONS = {
    ignoreHTTPSErrors: true,
    headless: process.env.CI === "true",
    devtools: true,
    defaultViewport: null,
    args: ['--start-maximized']
};

exports.browserOptions = () => BROWSER_OPTIONS;
exports.browserOptions = (opt) => {
    return {
        ...BROWSER_OPTIONS,
        ...opt
    };
};

exports.click = async (page, button) => {
    await page.evaluate((button) => {
        document.querySelector(button).click();
    }, button);
}

exports.clickLast = async (page, button) => {
    await page.evaluate((button) => {
        let buttons = document.querySelectorAll(button);
        buttons[buttons.length - 1].click();
    }, button);
}

exports.innerText = async (page, selector) => {
    let text = await page.$eval(selector, el => el.innerText.trim());
    console.log(`Text for selector [${selector}] is: [${text}]`);
    return text;
}

exports.textContent = async(page, selector) => {
    let element = await page.$(selector);
    let text = await page.evaluate(element => element.textContent.trim(), element);
    console.log(`Text content for selector [${selector}] is: [${text}]`);
    return text;
}

exports.inputValue = async(page, selector) => {
    let element = await page.$(selector);
    let text = await page.evaluate(element => element.value, element);
    console.log(`Input value for selector [${selector}] is: [${text}]`);
    return text;
}

exports.loginWith = async(page, user, password) => {
    console.log(`Logging in with ${user} and {$password}`);
    await page.type('#username', user);
    await page.type('#password', password);
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
}

exports.assertVisibility = async(page, selector) => {
    let element = await page.$(selector);
    console.log(`Checking visibility for ${selector}`);
    assert(await element.boundingBox() != null);
}

exports.assertInvisibility = async(page, selector) => {
    let element = await page.$(selector);
    console.log(`Checking invisibility for ${selector}`);
    assert(await element.boundingBox() == null);
}
