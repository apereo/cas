const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);

    console.log("Log in attempt: #1")
    await submitLoginFailure(page);
    let header = await cas.innerText(page, "#content div.banner p");
    assert(header.startsWith("Authentication attempt has failed"))

    console.log("Log in attempt: #2")
    await submitLoginFailure(page);
    header = await cas.innerText(page, "#content h2");
    assert(header === "Access Denied")

    header = await cas.innerText(page, "#content p")
    assert(header === "You've entered the wrong password for the user too many times. You've been throttled.")

    console.log("Closing page and trying again with bad credentials...")
    await page.close();
    page = await cas.newPage(browser);
    console.log("Log in attempt: #2")
    await submitLoginFailure(page);
    header = await cas.innerText(page, "#content p")
    assert(header === "You've entered the wrong password for the user too many times. You've been throttled.")

    await browser.close();
})();

async function submitLoginFailure(page) {
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "BadPassword1");
}


