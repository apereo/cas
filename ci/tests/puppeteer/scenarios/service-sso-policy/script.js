const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://google.com&renew=true");
    await cas.loginWith(page, "casuser", "Mellon");

    await cas.goto(page, "https://localhost:8443/cas");
    await cas.assertCookie(page, false);
    
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://github.com&renew=true");
    await cas.loginWith(page, "casuser", "Mellon");

    await cas.goto(page, "https://localhost:8443/cas");
    await cas.assertCookie(page);

    await browser.close();
})();
