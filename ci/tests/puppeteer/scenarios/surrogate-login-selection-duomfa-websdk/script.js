const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://example.org");
    await cas.loginWith(page, "+duobypass", "Mellon");
    await cas.assertTextContent(page, "#titlePanel h2", "Choose Account")
    await cas.assertTextContentStartsWith(page, "#surrogateInfo", "You are provided with a list of accounts");
    await cas.assertVisibility(page, '#surrogateTarget')
    await cas.assertVisibility(page, '#submit')
    await cas.assertVisibility(page, '#login')
    await page.select('#surrogateTarget', 'user3')
    await cas.click(page, "#submit")
    await page.waitForNavigation();
    await cas.assertTicketParameter(page);
    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(1000)
    await cas.assertTicketGrantingCookie(page);
    await cas.assertInnerTextStartsWith(page, "#content div p", "You, user3, have successfully logged in");
    await browser.close();
})();
