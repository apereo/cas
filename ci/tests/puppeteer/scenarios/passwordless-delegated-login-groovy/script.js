const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

async function submitUser(page, user) {
    await cas.type(page, '#username', user);
    await cas.pressEnter(page);
    await page.waitForNavigation();
    await page.waitForTimeout(2000)
}

(async () => {
    await cas.httpServer(__dirname, 5432, false);
    const browser = await puppeteer.launch(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        await cas.log("Checking for invalid/unknown user account");
        await cas.goto(page, "https://localhost:8443/cas/login");
        await submitUser(page, "none");
        await cas.assertInnerTextStartsWith(page, "#login div.banner",
            "Provided username cannot be recognized");

        await cas.log("Checking for password-eligible user account");
        await cas.goto(page, "https://localhost:8443/cas/login");
        await submitUser(page, "local");
        await cas.assertInvisibility(page, '#loginProviders');
        await cas.assertVisibility(page, '#password');

        await cas.log("Checking for user account with a single delegated client");
        await cas.goto(page, "https://localhost:8443/cas/login");
        await submitUser(page, "single-delegation");
        const url = await page.url();
        await cas.log(`Page url: ${url}`);
        assert(url.startsWith("https://httpbin.org/anything/cas3"));

        await cas.log("Checking for all-options user account");
        await cas.goto(page, "https://localhost:8443/cas/login");
        await submitUser(page, "all");
        await cas.assertVisibility(page, '#password');
        await cas.assertVisibility(page, '#loginProviders');
        await cas.assertVisibility(page, 'li #CasClient2');
        await cas.assertVisibility(page, 'li #CasClient3');
        await cas.assertInvisibility(page, 'li #CasClient1');

        await cas.log("Checking for unauthorized use of identity provider");
        const response = await cas.goto(page, "https://localhost:8443/cas/clientredirect?client_name=CasClient3");
        await cas.log(`${response.status()} ${response.statusText()}`);
        assert(response.status === 403);

        await cas.log("Checking for user account with multiple clients w/o password");
        await cas.goto(page, "https://localhost:8443/cas/login");
        await submitUser(page, "multi-delegation");
        await cas.assertInvisibility(page, '#password');
        await cas.assertVisibility(page, '#loginProviders');
        await cas.assertVisibility(page, 'li #CasClient2');
        await cas.assertVisibility(page, 'li #CasClient3');
        await cas.assertInvisibility(page, 'li #CasClient1')

    } finally {
        await browser.close();
        await process.exit(0);
    }
})();
