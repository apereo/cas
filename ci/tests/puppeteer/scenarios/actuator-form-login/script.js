const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true
    });
    const page = await browser.newPage();
    await page.goto("https://localhost:8443/cas/actuator/sso");

    var header = await page.$eval('#content h2', el => el.innerText)
    console.log(header)
    assert(header === "Login")

    let form = await page.$('#content form[name=fm1]');
    assert(await form.boundingBox() != null);

    let subtitle = await page.$eval('#content form[name=fm1] h3', el => el.innerText);
    console.log(subtitle);
    assert(subtitle === "Enter Username & Password");

    let uid = await page.$('#username');
    assert(await uid.boundingBox() != null);

    let pswd = await page.$('#password');
    assert(await pswd.boundingBox() != null);

    await browser.close();
})();
