const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await page.waitForTimeout(2000)
    await cas.assertInnerText(page, "#forgotPasswordLink", "Reset your password");
    
    await cas.click(page, "#forgotPasswordLink")
    await page.waitForTimeout(1000)
    await cas.assertInnerText(page, "#reset #fm1 h3", "Reset your password");
    await cas.assertVisibility(page, '#username')
    let uid = await page.$('#username');
    assert("none" === await uid.evaluate(el => el.getAttribute("autocapitalize")))
    assert("false" === await uid.evaluate(el => el.getAttribute("spellcheck")))
    assert("username" === await uid.evaluate(el => el.getAttribute("autocomplete")))

    await cas.type(page,'#username', "casuser");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await page.waitForTimeout(1000)

    await cas.assertInnerText(page, "#content h2", "Password Reset Instructions Sent Successfully.");
    await cas.assertInnerTextStartsWith(page, "#content p", "You should shortly receive a message");

    await page.goto("http://localhost:8282");
    await page.waitForTimeout(1000)
    await cas.click(page, "table tbody td a")
    await page.waitForTimeout(1000)

    let link = await cas.textContent(page, "div[name=bodyPlainText] .well");
    await page.goto(link);
    await page.waitForTimeout(1000)

    await cas.assertInnerText(page, "#content h2", "Answer Security Questions");

    await cas.type(page,'#q0', "answer1");
    await cas.type(page,'#q1', "answer2");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();
    await page.waitForTimeout(1000)

    await typePassword(page, "EaP8R&iX$eK4nb8eAI", "EaP8R&iX$eK4nb8eAI")
    await page.waitForTimeout(1000)
    await cas.assertInvisibility(page, '#password-confirm-mismatch-msg');
    await cas.assertInvisibility(page, '#password-policy-violation-msg');

    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await cas.assertInnerText(page, "#content h2", "Password Change Successful");
    await cas.assertInnerText(page, "#content p", "Your account password is successfully updated.");
    await browser.close();
})();

async function typePassword(page, pswd, confirm) {
    await cas.type(page,'#password', pswd);
    await cas.type(page,'#confirmedPassword', confirm);
}
