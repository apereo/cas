const puppeteer = require('puppeteer');
const assert = require('assert');
const cas = require('../../cas.js');
const fs = require('fs');
const request = require('request');

(async () => {
    const browserldap = await puppeteer.launch(cas.browserOptions());
    const pageldap = await cas.newPage(browserldap);
    await pageldap.goto("https://localhost:8443/cas/login");
    await cas.loginWith(pageldap, "aburr", "P@ssw0rd");
    await cas.assertTicketGrantingCookie(pageldap);
    const headerldap = await cas.innerText(pageldap, '#content div h2');
    assert(headerldap === "Log In Successful")
    const attributesldap = await cas.innerText(pageldap, '#attribute-tab-0 table#attributesTable tbody');
    assert(attributesldap.includes("aburr"))
    assert(attributesldap.includes("someattribute"))
    assert(attributesldap.includes("ldap-dn"))
    await browserldap.close();

    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await page.setRequestInterception(true);
    let args = process.argv.slice(2);
    let config = JSON.parse(fs.readFileSync(args[0]));
    assert(config != null)

    console.log("Certificate file: " + config.trustStoreCertificateFile);
    console.log("Private key file: " + config.trustStorePrivateKeyFile);

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

    const header = await cas.innerText(page, "#content div h2");
    assert(header === "Log In Successful")

    const body = await cas.innerText(page, '#content div p');
    assert(body.includes("1234567890@college.edu"))

    const attributes = await cas.innerText(page, '#attribute-tab-0 table#attributesTable tbody');
    assert(attributes.includes("casuserx509"))
    assert(attributes.includes("someattribute"))
    assert(attributes.includes("user-account-control"))

    await browser.close();
})();
