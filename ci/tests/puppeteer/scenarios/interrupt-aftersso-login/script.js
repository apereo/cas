const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, "https://apereo.github.io");
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
    await cas.assertInvisibility(page, "#cancel");
    await cas.submitForm(page, "#fm1");
    await cas.logPage(page);
    await page.waitForTimeout(1000);
    await cas.assertTicketParameter(page);

    await cas.log("Attempt to log into the application again with the same user");
    await cas.gotoLogin(page, "https://apereo.github.io");
    await cas.assertTicketParameter(page);

    await cas.gotoLogout(page);

    await cas.gotoLogin(page, "https://apereo.github.io");
    let url = await page.url();
    await cas.loginWith(page, "casblock", "Mellon");
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.assertMissingParameter(page, "ticket");
    await cas.assertPageUrl(page, url);

    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.assertTextContentStartsWith(page, "#content p", "The authentication flow has been interrupted");
    await cas.assertTextContent(page, "#interruptMessage", "You are blocked");
    await page.waitForTimeout(1000);
    await cas.logPage(page);
    await cas.assertCookie(page);
    await cas.assertInvisibility(page, "#fm1");

    await page.evaluate(() => {
        let execution = document.querySelector('#formlinks input[name=execution]').value;
        let content = document.querySelector('#content');
        content.innerHTML += `
        <form method="post" id="fmblocked">
            <input type="hidden" name="execution" value="${execution}"/>
            <input type="hidden" name="_eventId" value="proceed"/>
            <button name="proceed" id="proceed" type="submit" />
        </form>
        `;
    });
    await cas.submitForm(page, "#fmblocked", undefined, 200);
    await page.waitForTimeout(1000);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await cas.logPage(page);

    await browser.close();
})();
