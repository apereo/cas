const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());

    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)
    await cas.assertCookie(page);

    await cas.goto(page, "https://localhost:8443/cas/account");
    await page.waitForTimeout(1000)

    await cas.click(page, "#linkOverview")
    await page.waitForTimeout(1000)

    await cas.click(page, "#linkAttributes")
    await page.waitForTimeout(1000)

    await cas.click(page, "#linkServices")
    await page.waitForTimeout(1000)

    await cas.click(page, "#linkSecurityQuestions")
    await page.waitForTimeout(1000)
    await cas.assertVisibility(page, "#securityQuestionsTable")

    await cas.click(page, "#linkAuditLog")
    await page.waitForTimeout(1000)

    await cas.click(page, "#linkPasswordManagement")
    await page.waitForTimeout(1000)
    await cas.assertCookie(page, false);
    
    await browser.close();
})();
