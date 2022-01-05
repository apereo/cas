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
    let exec = await cas.launchSamlSp(idpMetadataPath, samlSpDir);
    await cas.waitFor('https://localhost:9876/sp/saml/status', async () => {
        const browser = await puppeteer.launch(cas.browserOptions());
        const page = await cas.newPage(browser);

        let token = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMTkyQ0JDLUhTMzg0IiwiYWxnIjoiZGlyIn0..zTe4qLwuyvVi6gHmSL4VcQ.NAmUxtxfEc-xJ0BO1DIUtxRN05-XTiB4bFVqN4YFvBPtVUrppTR5oXVKszWYQD_jWuhHnUBCVcvOBri9n-q6rKXQBkMnW9TLXQ4d9waJUOKpxORvgX3T56qoVfYTbUkVxe-5VchX000JWy8GdhpyWawldG0au03GhU7jhVnQeMlb7WWaNFOGXQwx6wvF0B30UL-6wgZO1nWD7InaQXJiFZazE0HK0DX61DUbP6PFYJKOBkbSWg9vSSzCeTxVFx9uJXMSAg9_vacpAYmq02ixV8e73CU9_hHhiCqEOYGunzwO4mEm6mn3fhPz6Q5azzPhZTc-lROKc66bmo_Y7jtMRmwgwBTq4diY9bIhw0x_qMsLLEdk1qk-dH9_FhBcunW2PkM8rCyGyDQ2slL-Axs_zg.HgP2g-UNsljcDhO74OMFWKxkYiSo4mbK";

        let entityId = "https://spring.io/security/saml-sp";
        let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
        url += `?providerId=${entityId}&token=${token}`;

        console.log(`Navigating to ${url}`);
        await page.goto(url);
        await page.waitForTimeout(5000)

        let resultUrl = await page.url()
        await cas.logg(`Page url: ${resultUrl}`)
        assert(resultUrl === "https://localhost:9876/sp/")
        await cas.assertInnerText(page, "#principal", "casuser")

        await page.goto("https://localhost:8443/cas/login");
        await cas.assertTicketGrantingCookie(page);
        await cas.assertInnerText(page, '#content div h2', "Log In Successful");
        await page.waitForTimeout(1000);
        
        await browser.close();
        await cleanUp(exec);
    }, async error => {
        await cleanUp(exec);
        console.log(error);
        throw error;
    })
})();

