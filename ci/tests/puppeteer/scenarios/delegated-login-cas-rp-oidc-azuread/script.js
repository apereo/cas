const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://apereo.github.io";
    
    const url = `https://localhost:8443/cas/login?service=${service}`;
    await cas.goto(page, url);

    await cas.assertVisibility(page, 'li #AzureClient');
    await cas.click(page, "li #AzureClient");
    await page.waitForNavigation();
    await page.waitForTimeout(4000);
    await cas.screenshot(page);

    let username = `castest@${process.env.AZURE_AD_DOMAIN}`;
    await cas.type(page, "input[name=loginfmt]", username, true);
    await cas.pressEnter(page);
    
    await page.waitForTimeout(3000);
    await cas.type(page, "input[name=passwd]", process.env.AZURE_AD_USER_PASSWORD, true);
    await cas.pressEnter(page);
    await page.waitForTimeout(4000);
    await cas.screenshot(page);
    await cas.click(page, "#idBtn_Back");
    await page.waitForTimeout(7000);

    await cas.log(`Page URL: ${page.url()}`);
    let result = new URL(page.url());
    await cas.log(result.searchParams.toString());
    assert(result.searchParams.has("ticket") === true);
    let ticket = result.searchParams.get("ticket");
    let body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    let json = JSON.parse(body);
    let authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.name[0] === "CAS Test");
    assert(authenticationSuccess.attributes.preferred_username[0] === username);

    await browser.close();
})();
