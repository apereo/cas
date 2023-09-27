const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.goto(page, "http://localhost:8080/cas/login");
    await cas.assertVisibility(page, "#drawerButton");
    await cas.click(page, "#drawerButton");
    await page.waitForTimeout(1000);
    await cas.assertVisibility(page, "#halbrowser");
    await cas.click(page, "#halbrowser");
    await page.waitForTimeout(3000);
    await cas.screenshot(page);
    await cas.assertVisibility(page, "div.container-fluid");
    await cas.assertVisibility(page, "div#HttpRequestTrigger");

    await cas.gotoLogin(page);
    await page.focus("#username");
    await page.keyboard.press("Tab");
    await page.focus("#password");
    await page.keyboard.press("Tab");

    await cas.assertVisibility(page, "#usernameValidationMessage");
    await cas.assertVisibility(page, "#passwordValidationMessage");

    await cas.loginWith(page);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");

    await page.waitForTimeout(1000);
    assert (await cas.pageVariable(page, "googleAnalyticsTrackingId") !== null);

    await cas.gotoLogout(page);
    await cas.gotoLogin(page, "https://anything-matches-here");
    await cas.assertVisibility(page, "#wildcardService");

    await browser.close();
})();
