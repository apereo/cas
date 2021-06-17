const puppeteer = require('puppeteer');
const assert = require('assert');
const fs = require('fs');
const path = require('path');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.goto("https://samltest.id/upload.php");

    const entityIds = [
        "https://studypages.com/saml-sp",
        "https://aca.ucop.edu",
        "https://www.peoplegrove.com/saml",
        "https://login.at.internet2.edu/Saml2/proxy_saml2_backend.xml",
        "https://uchicago.infoready4.com/shibboleth",
        "https://cole.uconline.edu/shibboleth-sp"
    ];

    for (const entityId of entityIds) {
        let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
        url += `?providerId=${entityId}`;
        url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";

        console.log("Navigating to " + url);
        await page.goto(url);
        await page.waitForTimeout(1000);
        await cas.assertVisibility(page, '#username')
        await cas.assertVisibility(page, '#password')
    }
    let metadataDir = path.join(__dirname, '/saml-md');
    fs.rmdirSync(metadataDir, { recursive: true });
    
    await browser.close();
})();


