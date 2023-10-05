const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://apereo.github.io";
    
    const url = `https://localhost:8443/cas/login?service=${service}`;
    await cas.goto(page, url);

    await cas.assertVisibility(page, 'li #Keycloak');
    await cas.click(page, "li #Keycloak");
    await page.waitForNavigation();
    await page.waitForTimeout(3000);
    await cas.screenshot(page);
    await cas.loginWith(page, "caskeycloak", "r2RlZXz6f2h5");
    await page.waitForTimeout(1000);

    await cas.logPage(page);
    let result = new URL(page.url());
    await cas.log(result.searchParams.toString());

    assert(result.searchParams.has("ticket") === true);

    let ticket = result.searchParams.get("ticket");
    let body = await cas.doRequest(`https://localhost:8443/cas/p3/serviceValidate?service=${service}&ticket=${ticket}&format=JSON`);
    await cas.log(body);
    let json = JSON.parse(body);
    let authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "caskeycloak@example.org");
    assert(authenticationSuccess.attributes.name != null);
    assert(authenticationSuccess.attributes.email != null);
    assert(authenticationSuccess.attributes.department != null);
    assert(authenticationSuccess.attributes.cas_role != null);
    
    await browser.close();
})();
