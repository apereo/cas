const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.loginWith(page);
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertTextContent(page, "#interruptMessage", "We interrupted your login");
    await cas.assertCookie(page);
    await cas.assertVisibility(page, '#interruptLinks');
    await cas.assertVisibility(page, '#attributesTable');
    await cas.assertVisibility(page, '#field1');
    await cas.assertVisibility(page, '#field1-value');
    await cas.assertVisibility(page, '#field2');
    await cas.assertVisibility(page, '#field2-value');

    let cancel = await page.$('#cancel');
    assert(cancel == null);

    await cas.submitForm(page, "#fm1");
    
    await browser.close();
})();
