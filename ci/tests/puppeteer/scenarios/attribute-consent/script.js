const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login?service=https://example.org");

    await cas.loginWith(page, "casuser", "Mellon");

    let header = await cas.textContent(page, '#content h2')
    assert(header === "Attribute Consent")

    header = await cas.textContent(page, "#appTitle");
    assert(header === "The following attributes will be released to [https://example.org]:")

    header = await cas.textContent(page, "#first-name");
    assert(header === "first-name")

    header = await cas.textContent(page, "#first-name-value");
    assert(header === "[Apereo]")

    header = await cas.textContent(page, "#last-name");
    assert(header === "last-name")

    header = await cas.textContent(page, "#last-name-value");
    assert(header === "[CAS]")

    header = await cas.textContent(page, "#email");
    assert(header === "email")

    header = await cas.textContent(page, "#email-value");
    assert(header === "[casuser@example.org]")

    await cas.click(page, "#optionsButton");
    await page.waitForTimeout(1000)

    let opt = await page.$('#optionAlways');
    assert(opt != null);
    opt = await page.$('#optionAttributeName');
    assert(opt != null);
    opt = await page.$('#optionAttributeValue');
    assert(opt != null);

    header = await cas.textContent(page, "#reminderTitle");
    assert(header === "How often should I be reminded to consent again?")

    opt = await page.$('#reminder');
    assert(opt != null);

    opt = await page.$('#reminderTimeUnit');
    assert(opt != null);

    opt = await page.$('#confirm');
    assert(opt != null);

    opt = await page.$('#cancel');
    assert(opt != null);

    const url = "https://localhost:8443/cas/actuator/attributeConsent/casuser"
    console.log("Trying " + url)
    const response = await page.goto(url);
    console.log(response.status() + " " + response.statusText())
    assert(response.ok())

    await browser.close();
})();


