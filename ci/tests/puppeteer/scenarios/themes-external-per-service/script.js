const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Trying first app with a fancy theme");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://apereo.github.io");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);

    await cas.log("Listing all stylesheet links for first app");
    await page.evaluate(() => {
        const links = document.querySelectorAll("link[rel=stylesheet]");
        links.forEach(lnk => console.log(lnk.getAttribute("href")));
    });

    await cas.assertInvisibility(page, "#username");
    await cas.assertInvisibility(page, "#password");

    await cas.log("Trying second app with a fancy theme");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://localhost:9859/anything/fancy");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);

    await cas.log("Listing all stylesheet links for second app");
    await page.evaluate(() => {
        const links = document.querySelectorAll("link[rel=stylesheet]");
        links.forEach(lnk => console.log(lnk.getAttribute("href")));
    });
    await cas.assertInvisibility(page, "#username");
    await cas.assertInvisibility(page, "#password");

    await cas.log("Trying third app with a default theme");
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://localhost:9859/anything/default");
    await page.waitForTimeout(2000);
    await cas.screenshot(page);
    await cas.log("Listing all stylesheet links for third app");
    await page.evaluate(() => {
        const links = document.querySelectorAll("link[rel=stylesheet]");
        links.forEach(lnk => console.log(lnk.getAttribute("href")));
    });
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");

    await browser.close();
})();
