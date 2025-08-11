
const assert = require("assert");
const cas = require("../../cas.js");
const fs = require("fs");
const request = require("request");

(async () => {
    let browser = await cas.newBrowser(cas.browserOptions());
    let page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "aburr", "P@ssw0rd");
    await cas.assertCookie(page);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    const attributesldap = await cas.innerText(page, "#attribute-tab-0 table#attributesTable tbody");
    assert(attributesldap.includes("aburr"));
    assert(attributesldap.includes("someattribute"));
    assert(attributesldap.includes("ldap-dn"));
    await cas.closeBrowser(browser);

    browser = await cas.newBrowser(cas.browserOptions());
    page = await cas.newPage(browser);

    await page.setRequestInterception(true);
    const args = process.argv.slice(2);
    const config = JSON.parse(fs.readFileSync(args[0]));

    await cas.log(`Certificate file: ${config.trustStoreCertificateFile}`);
    await cas.log(`Private key file: ${config.trustStorePrivateKeyFile}`);

    const cert = fs.readFileSync(config.trustStoreCertificateFile);
    const key = fs.readFileSync(config.trustStorePrivateKeyFile);

    page.on("request", (interceptedRequest) => {
        const options = {
            uri: interceptedRequest.url(),
            method: interceptedRequest.method(),
            headers: interceptedRequest.headers(),
            body: interceptedRequest.postData(),
            cert: cert,
            key: key
        };

        request(options, (err, resp, body) => {
            if (err) {
                cas.logr(`Unable to call ${options.uri}`, err);
                return interceptedRequest.abort("connectionrefused");
            }

            interceptedRequest.respond({
                status: resp.statusCode,
                contentType: resp.headers["content-type"],
                headers: resp.headers,
                body: body
            });
        });

    });

    await cas.gotoLogin(page);
    await cas.sleep(5000);

    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertInnerTextContains(page, "#content div p", "1234567890@college.edu");

    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "casuserx509");
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "someattribute");
    await cas.assertInnerTextContains(page, "#attribute-tab-0 table#attributesTable tbody", "user-account-control");
    await cas.assertInnerTextDoesNotContain(page, "#attribute-tab-0 table#attributesTable tbody", "shouldntbehere");

    await cas.closeBrowser(browser);
})();
