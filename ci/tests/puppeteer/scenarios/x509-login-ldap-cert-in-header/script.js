
const assert = require("assert");
const cas = require("../../cas.js");
const fs = require("fs");

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
    assert(attributesldap.includes("uid"));
    await cas.closeBrowser(browser);

    browser = await cas.newBrowser(cas.browserOptions());
    page = await cas.newPage(browser);

    await page.setRequestInterception(true);

    const args = process.argv.slice(2);
    const config = JSON.parse(fs.readFileSync(args[0]));

    await cas.log(`Certificate file: ${config.trustStoreCertificateFile}`);

    const certBuffer = fs.readFileSync(config.trustStoreCertificateFile);
    const certHeader = certBuffer.toString().replace(/\n/g, " ").replace(/\r/g,"");

    await cas.log(`ssl-client-cert-from-proxy: ${certHeader}`);

    page.on("request", (request) => {
        const data = {
            "method": "GET",
            "headers": {
                ...request.headers(),
                "ssl-client-cert-from-proxy": certHeader
            }
        };
        request.continue(data);
    });

    await cas.gotoLogin(page);
    await cas.sleep(5000);

    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertInnerTextContains(page, "#content div p", "1234567890@college.edu");

    await cas.gotoLogin(page, "https://localhost:9859/anything/cas");
    await cas.sleep(5000);
    await assertFailure(page);
    await cas.closeBrowser(browser);
})();

async function assertFailure(page) {
    await cas.assertInnerText(page, "#loginErrorsPanel p", "Service access denied due to missing privileges.");
    await cas.sleep(1000);
}

