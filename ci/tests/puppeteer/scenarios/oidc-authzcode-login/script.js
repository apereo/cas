const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const url = "https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=client&scope=openid%20MyCustomScope&redirect_uri=http://localhost:8080/oidclogin&nonce=3d3a7457f9ad3&state=1735fd6c43c14&claims=%7B%22userinfo%22%3A%20%7B%20%22name%22%3A%20%7B%22essential%22%3A%20true%7D%2C%22phone_number%22%3A%20%7B%22essential%22%3A%20true%7D%7D%7D";
    await page.goto(url);

    await page.waitForTimeout(1000)
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(1000)
    await cas.assertVisibility(page, '#userInfoClaims')
    await cas.assertVisibility(page, '#scopes')
    await cas.assertVisibility(page, '#MyCustomScope')
    await cas.assertVisibility(page, '#openid')
    await cas.assertVisibility(page, '#informationUrl')
    await cas.assertVisibility(page, '#privacyUrl')
    await cas.assertVisibility(page, '#name')
    await cas.assertVisibility(page, '#phone_number')

    await browser.close();
})();
