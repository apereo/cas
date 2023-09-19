const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

async function loginWith(page, user, password) {
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, user, password);
    await page.waitForTimeout(1000);
    await cas.assertInnerText(page, "#content h2", `This account has been locked.`);
    await cas.assertVisibility(page, "#captchaImage");
    await cas.assertVisibility(page, "#captchaValue");
    await cas.assertVisibility(page, "#captcha");
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await loginWith(page, "casuser", "Mellon");
    let captcha = await cas.innerText(page, "#captcha");
    await cas.type(page, "#captchaValue", captcha);
    await cas.submitForm(page, "#fm1");
    await page.waitForTimeout(1000);
    await cas.assertInnerText(page, "#content h2", "Your account is now unlocked.");
    await page.waitForTimeout(1000);
    await cas.click(page, "#loginbtn");
    await page.waitForNavigation();
    let url = await page.url();
    await cas.log(url);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await browser.close();
})();
