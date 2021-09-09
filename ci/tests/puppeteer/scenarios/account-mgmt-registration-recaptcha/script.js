const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");

    await page.waitForTimeout(2000)
    await cas.assertTextContent(page, "#accountSignUpLink", "Sign Up")
    await cas.submitForm(page, "#accountMgmtSignupForm")
    await page.waitForTimeout(1000)

    await cas.assertInnerText(page, '#content h2', "Account Registration");
    
    await cas.type(page,'#username', "casuser");
    await cas.type(page,'#firstName', "CAS");
    await cas.type(page,'#lastName', "Person");
    await cas.type(page,'#email', "cas@example.org");
    await cas.type(page,'#phone', "+1 347 745 4321");
    await cas.click(page, "#submit")
    await page.waitForNavigation();
    await page.waitForTimeout(1000)
    await cas.assertTextContent(page, "div .banner-danger p", "reCAPTCHA validation failed.")
    await browser.close();
})();

