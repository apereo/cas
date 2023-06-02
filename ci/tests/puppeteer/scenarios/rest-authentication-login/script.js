const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    await cas.httpServer(__dirname, 5432);
    
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "https://localhost:8443/cas/login");
    await cas.loginWith(page, "restapi", "YdCP05HvuhOH^*Z");
    await cas.assertCookie(page);
    await browser.close();
    await process.exit(0);
})();
