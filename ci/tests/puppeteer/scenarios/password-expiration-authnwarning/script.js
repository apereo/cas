const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await cas.loginWith(page, "casuser", "Mellon");

    await page.waitForTimeout(1000)

    let header = await cas.textContent(page, "#content h1");

    assert(header === "Authentication Succeeded with Warnings")

    await cas.assertVisibility(page, '#changePassword')

    await cas.submitForm(page, "#changePasswordForm");

    header = await cas.textContent(page, "#pwdmain h3");

    assert(header === "Hello, casuser. You must change your password.")

    await typePassword(page, "123456", "123456")
    await page.waitForTimeout(1000)
    await cas.assertVisibility(page, '#password-policy-violation-msg');

    await typePassword(page, "123456", "123")
    await page.waitForTimeout(1000)
    await cas.assertVisibility(page, '#password-confirm-mismatch-msg');

    await typePassword(page, "Testing1234", "Testing1234")
    await page.waitForTimeout(1000)
    await cas.assertVisibility(page, '#password-strength-msg');
    await cas.assertVisibility(page, '#password-strength-notes');

    await typePassword(page, "EaP8R&iX$eK4nb8eAI", "EaP8R&iX$eK4nb8eAI")
    await page.waitForTimeout(1000)
    await cas.assertInvisibility(page, '#password-confirm-mismatch-msg');
    await cas.assertInvisibility(page, '#password-policy-violation-msg');

    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    header = await cas.textContent(page, "#content h2");
    assert(header === "Password Change Successful");

    header = await cas.textContent(page, "#content p");
    assert(header === "Your account password is successfully updated.")

    await cas.submitForm(page, "#form");

    let element = await cas.innerText(page, '#content div h2');
    assert(element === "Log In Successful")

    await cas.assertTicketGrantingCookie(page);

    await page.waitForTimeout(1000)
    await browser.close();
})();


async function typePassword(page, pswd, confirm) {
    await cas.type(page,'#password', pswd);
    await cas.type(page,'#confirmedPassword', confirm);
}
