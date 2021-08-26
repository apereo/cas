const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');
const fs = require('fs');
const request = require('request');

(async () => {
    let browser = await puppeteer.launch(cas.browserOptions());
    let page = await cas.newPage(browser);
    await page.goto("https://localhost:8443/cas/login");
    await cas.loginWith(page, "aburr", "P@ssw0rd");
    await cas.assertTicketGrantingCookie(page);
    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    const attributesldap = await cas.innerText(page, '#attribute-tab-0 table#attributesTable tbody');
    assert(attributesldap.includes("aburr"))
    assert(attributesldap.includes("someattribute"))
    assert(attributesldap.includes("ldap-dn"))
    await browser.close();

    browser = await puppeteer.launch(cas.browserOptions());
    page = await cas.newPage(browser);

    await page.setRequestInterception(true);
    let args = process.argv.slice(2);
    let config = JSON.parse(fs.readFileSync(args[0]));
    assert(config != null)

    console.log(`Certificate file: ${config.trustStoreCertificateFile}`);
    console.log(`Private key file: ${config.trustStorePrivateKeyFile}`);

    const cert = fs.readFileSync(config.trustStoreCertificateFile);
    const key = fs.readFileSync(config.trustStorePrivateKeyFile);

    page.on('request', interceptedRequest => {
        const options = {
            uri: interceptedRequest.url(),
            method: interceptedRequest.method(),
            headers: interceptedRequest.headers(),
            body: interceptedRequest.postData(),
            cert: cert,
            key: key
        };

        request(options, function (err, resp, body) {
            if (err) {
                console.error(`Unable to call ${options.uri}`, err);
                return interceptedRequest.abort('connectionrefused');
            }

            interceptedRequest.respond({
                status: resp.statusCode,
                contentType: resp.headers['content-type'],
                headers: resp.headers,
                body: body
            });
        });

    });

    await page.goto("https://localhost:8443/cas/login");
    await page.waitForTimeout(5000)

    await cas.assertInnerText(page, '#content div h2', "Log In Successful");
    await cas.assertInnerTextContains(page, "#content div p", "1234567890@college.edu");

    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "casuserx509");
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "someattribute");
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "user-account-control");

    await browser.close();
})();
