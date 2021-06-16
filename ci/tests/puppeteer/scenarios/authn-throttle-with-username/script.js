const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);
    await submitLoginFailure(page);

    let header = await cas.innerText(page, "#content div.banner p");
    assert(header.startsWith("Authentication attempt has failed"))

    await submitLoginFailure(page);

    header = await cas.innerText(page, "#content h2");
    assert(header === "Access Denied")

    header = await cas.innerText(page, "#content p")
    assert(header === "You've entered the wrong password for the user too many times. You've been throttled.")
    
    const url = "https://localhost:8443/cas/actuator/throttles"
    console.log("Trying " + url)
    const response = await page.goto(url);
    console.log(response.status() + " " + response.statusText())
    assert(response.ok())

    await browser.close();
})();

async function submitLoginFailure(page) {
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "casuser", "BadPassword1");
}


