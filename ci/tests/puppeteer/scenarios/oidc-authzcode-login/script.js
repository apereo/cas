const puppeteer = require('puppeteer');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch({
        ignoreHTTPSErrors: true,
        headless: true
    });
    const page = await browser.newPage();
    const url = "https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=openid%20MyCustomScope&redirect_uri=http://localhost:8080/oidclogin&nonce=3d3a7457f9ad3&state=1735fd6c43c14&claims=%7B%22userinfo%22%3A%20%7B%20%22name%22%3A%20%7B%22essential%22%3A%20true%7D%2C%22phone_number%22%3A%20%7B%22essential%22%3A%20true%7D%7D%7D";
    await page.goto(url);

    await page.type('#username', "casuser");
    await page.type('#password', "Mellon");
    await page.keyboard.press('Enter');
    await page.waitForNavigation();

    await page.waitForTimeout(2000)

    let userInfoClaims = await page.$('#userInfoClaims');
    assert(await userInfoClaims.boundingBox() != null);

    let scopes = await page.$('#scopes');
    assert(await scopes.boundingBox() != null);

    let customScope = await page.$('#MyCustomScope');
    assert(await customScope.boundingBox() != null);

    let openid = await page.$('#openid');
    assert(await openid.boundingBox() != null);

    let infoUrl = await page.$('#informationUrl');
    assert(await infoUrl.boundingBox() != null);

    let privacyUrl = await page.$('#privacyUrl');
    assert(await privacyUrl.boundingBox() != null);

    let claimName = await page.$('#name');
    assert(await claimName.boundingBox() != null);

    let phoneNumber = await page.$('#phone_number');
    assert(await phoneNumber.boundingBox() != null);

    await browser.close();
})();
