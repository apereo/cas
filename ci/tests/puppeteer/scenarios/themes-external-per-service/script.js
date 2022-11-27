const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    console.log("Trying first app with a fancy theme");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    await cas.assertInvisibility(page, "#username");
    await cas.assertInvisibility(page, "#password");

    console.log("Trying second app with a fancy theme");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://httpbin.org/anything/fancy");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    await cas.assertInvisibility(page, "#username");
    await cas.assertInvisibility(page, "#password");

    console.log("Trying third app with a default theme");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://httpbin.org/anything/default");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");

    await browser.close();
})();
