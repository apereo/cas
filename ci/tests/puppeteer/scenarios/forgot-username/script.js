const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.assertTextContent(page, "#forgotUsernameLink", "Forgot your username?");

    await cas.click(page, "#forgotUsernameLink");
    await page.waitForTimeout(1000);

    await cas.assertTextContent(page, "#reset #fm1 h3",  "Forgot your username?");
    await cas.assertVisibility(page, '#email');

    await cas.type(page,'#email', "casuser@example.org");
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await cas.assertInnerTextStartsWith(page, "#content h2", "Instructions Sent Successfully.");
    await cas.assertInnerTextStartsWith(page, "#content p", "You should shortly receive a message");

    await browser.close();
})();

