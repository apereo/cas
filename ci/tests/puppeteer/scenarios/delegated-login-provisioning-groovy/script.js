const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const fs = require('fs');
const os = require('os');
const assert = require('assert');
const path = require('path');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page, "https://apereo.github.io");
    await cas.click(page, "li #CasClient");
    await page.waitForNavigation();
    await cas.loginWith(page);
    await page.waitForTimeout(1000);
    await cas.assertTicketParameter(page);

    let result = path.join(os.tmpdir(), "profile.txt");
    await cas.log(`Target file should be at ${result}`);
    assert(fs.existsSync(result) === true);
    
    await browser.close();
})();
