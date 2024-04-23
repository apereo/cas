
const cas = require("../../cas.js");

async function loginWith(page, user, password) {
    await cas.gotoLogin(page);
    await cas.loginWith(page, user, password);
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content h2", "This account has been locked.");
    await cas.assertVisibility(page, "#captchaImage");
    await cas.assertVisibility(page, "#captchaValue");
    await cas.assertVisibility(page, "#captcha");
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await loginWith(page, "casuser", "Mellon");
    const captcha = await cas.innerText(page, "#captcha");
    await cas.type(page, "#captchaValue", captcha);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content h2", "Your account is now unlocked.");
    await cas.sleep(1000);
    await cas.click(page, "#loginbtn");
    await cas.waitForNavigation(page);
    await cas.logPage(page);
    await cas.loginWith(page);
    await cas.assertCookie(page);
    await browser.close();
})();
