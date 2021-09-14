const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');
const assert = require("assert");

async function cleanUp(exec) {
    console.log("Killing SAML2 SP process...");
    exec.kill();
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await cas.removeDirectory(path.join(__dirname, '/saml-sp'));
}

(async () => {
    let samlSpDir = path.join(__dirname, '/saml-sp');
    let idpMetadataPath = path.join(__dirname, '/saml-md/idp-metadata.xml');
    let exec = await cas.launchSamlSp(idpMetadataPath, samlSpDir, ['-DacsUrl=https://httpbin.org/post', '-DsignAuthnRequests=true']);
    await cas.waitFor('https://localhost:9876/sp/saml/status', async function () {
        const browser = await puppeteer.launch(cas.browserOptions());
        const page = await cas.newPage(browser);

        console.log("Trying without an exising SSO session...")
        page.goto("https://localhost:9876/sp")
        await page.waitForTimeout(3000)
        await page.waitForSelector('#idpForm', {visible: true});
        await cas.submitForm(page, "#idpForm");
        await page.waitForTimeout(2000)
        await page.waitForSelector('#username', {visible: true});
        await cas.loginWith(page, "casuser", "Mellon");
        await page.waitForResponse(response => response.status() === 200)
        await page.waitForTimeout(3000)
        console.log(`Page URL: ${page.url()}`);
        await page.waitForSelector('body pre', { visible: true });
        let content = await cas.textContent(page, "body pre");
        let payload = JSON.parse(content);
        console.log(payload);
        assert(payload.form.SAMLResponse !== null);
        
        console.log("Trying with an exising SSO session...")
        await page.goto("https://localhost:8443/cas/logout");
        await page.goto("https://localhost:8443/cas/login");
        await cas.loginWith(page, "casuser", "Mellon");
        await cas.assertTicketGrantingCookie(page);
        page.goto("https://localhost:9876/sp")
        await page.waitForTimeout(3000)
        await page.waitForSelector('#idpForm', {visible: true});
        await cas.submitForm(page, "#idpForm");
        await page.waitForTimeout(3000)
        console.log(`Page URL: ${page.url()}`);
        await page.waitForSelector('body pre', { visible: true });
        content = await cas.textContent(page, "body pre");
        payload = JSON.parse(content);
        console.log(payload);
        assert(payload.form.SAMLResponse !== null);

        await browser.close();
        await cleanUp(exec);
    }, async function (error) {
        await cleanUp(exec);
        console.log(error);
        throw error;
    })
})();

