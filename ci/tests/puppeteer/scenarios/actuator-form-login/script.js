const puppeteer = require("puppeteer");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.goto(page, "https://localhost:8443/cas/actuator/info");

    await cas.assertInnerText(page, "#content h2", "Login");
    await cas.assertVisibility(page, "#content form[name=fm1]");

    await cas.assertInnerText(page, "#content form[name=fm1] h3", "Enter Username & Password");
    await cas.assertVisibility(page, "#username");

    await cas.attributeValue(page, "#username", "autocapitalize", "none");
    await cas.attributeValue(page, "#username", "spellcheck", "false");
    await cas.attributeValue(page, "#username", "autocomplete", "username");
    await cas.assertVisibility(page, "#password");

    await cas.loginWith(page, "unknown", "badpassword");
    await cas.waitForTimeout(page, 1000);

    await cas.screenshot(page);
    await cas.assertVisibility(page, "#errorPanel");
    await cas.assertInnerText(page, "#errorPanel", "Invalid credentials.");

    await cas.loginWith(page);
    await cas.screenshot(page);
    const content = await page.content();
    const json = await cas.substring(content, "<pre>", "</pre>");
    const payload = JSON.parse(json);
    await cas.log(payload);

    await browser.close();
})();
