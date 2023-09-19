const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.goto(page, "https://localhost:8443/cas/login?service=https://github.com");
    await page.waitForTimeout(1000);

    let loginProviders = await page.$('#loginProviders');
    assert(loginProviders == null);

    await cas.goto(page, "https://localhost:8443/cas/login?service=https://google.com");
    await page.waitForTimeout(1000);

    await cas.assertVisibility(page, 'li #CASServerOne');
    await cas.assertVisibility(page, 'li #CASServerTwo');
    assert(await page.$('#username') == null);
    assert(await page.$('#password') == null);


    const baseUrl = "https://localhost:8443/cas/actuator/registeredServices";
    await cas.doGet(baseUrl, res => {
        assert(res.status === 200);
        const length = res.data[1].length;
        cas.log(`Services found: ${length}`);
        assert(length === 2);
        res.data[1].forEach(service => {
            assert(service.accessStrategy !== undefined);
            assert(service.accessStrategy.delegatedAuthenticationPolicy !== undefined);
        });
    }, err => {
        throw err;
    }, {
        'Content-Type': 'application/json'
    });
    
    await browser.close();
})();


