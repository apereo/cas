const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "Mellon")

    await page.goto("https://localhost:8443/cas/login?service=https://example.org");
    await cas.assertTextContent(page, '#content h2', "Attribute Consent");
    await cas.assertTextContent(page, "#appTitle", "The following attributes will be released to [https://example.org]:")
    await cas.assertTextContent(page, "#first-name", "first-name");
    await cas.assertTextContent(page, "#first-name-value", "[Apereo]");
    await cas.assertTextContent(page, "#last-name", "last-name");
    await cas.assertTextContent(page, "#last-name-value", "[CAS]");
    await cas.assertTextContent(page, "#email", "email");
    await cas.assertTextContent(page, "#email-value", "[casuser@example.org]");

    await cas.screenshot(page);
    await cas.click(page, "#optionsButton");
    await page.waitForTimeout(2000)

    await cas.screenshot(page);
    let opt = await page.$('#optionAlways');
    assert(opt != null);
    opt = await page.$('#optionAttributeName');
    assert(opt != null);
    opt = await page.$('#optionAttributeValue');
    assert(opt != null);
    await cas.assertTextContent(page, "#reminderTitle", "How often should I be reminded to consent again?")

    opt = await page.$('#reminder');
    assert(opt != null);
    opt = await page.$('#reminderTimeUnit');
    assert(opt != null);
    opt = await page.$('#confirm');
    assert(opt != null);
    opt = await page.$('#cancel');
    assert(opt != null);
    const url = "https://localhost:8443/cas/actuator/attributeConsent/casuser"
    console.log(`Trying ${url}`)
    const response = await page.goto(url);
    console.log(`${response.status()} ${response.statusText()}`)
    assert(response.ok())

    await browser.close();
})();


