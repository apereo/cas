const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    for (let i = 0; i < 5; i++) {
        await cas.gotoLogout(page);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    }
    await browser.close();
})();
