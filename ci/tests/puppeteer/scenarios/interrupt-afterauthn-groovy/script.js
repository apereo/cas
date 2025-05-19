
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "testuser", "testuser");
    await cas.assertTextContent(page, "#content h1", "Authentication Interrupt");
    await cas.sleep(1000);
    await cas.submitForm(page, "#fm1");
    await cas.assertTextContent(page, "#content h1", "Authentication Succeeded with Warnings");
    await cas.sleep(1000);
    await cas.submitForm(page, "#form");
    await cas.assertCookie(page);
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.gotoLogout(page);

    await cas.gotoLogin(page);
    await cas.loginWith(page, "blockuser", "blockuser");
    await cas.sleep(1000);
    await cas.assertInvisibility(page, "#proceed");
    await cas.click(page, "#link1");
    await cas.sleep(2000);
    await cas.logPage(page);
    const url = await page.url();
    assert(url.includes("https://apereo.github.io"));
    await cas.gotoLogout(page);

    await cas.gotoLogin(page);
    await cas.loginWith(page, "blockuser", "blockuser");
    await cas.sleep(1000);
    await cas.assertInvisibility(page, "#proceed");
    await page.locator("div ::-p-text(Test link with special symbols: é & @)").click();
    await cas.sleep(2000);
    await cas.logPage(page);
    const url2 = await page.url();
    assert(url2.includes("https://localhost:9859/anything/cas"));
    await cas.gotoLogout(page);
    
    await browser.close();
})();
