
const path = require("path");
const cas = require("../../cas.js");
const assert = require("assert");

async function cleanUp() {
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.goto(page, "https://localhost:8443/cas/idp/metadata");
    await cas.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());

    await cas.waitFor("https://localhost:9876/sp/saml/status", async () => {

        const token = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMTkyQ0JDLUhTMzg0IiwiYWxnIjoiZGlyIn0..zTe4qLwuyvVi6gHmSL4VcQ.NAmUxtxfEc-xJ0BO1DIUtxRN05-XTiB4bFVqN4YFvBPtVUrppTR5oXVKszWYQD_jWuhHnUBCVcvOBri9n-q6rKXQBkMnW9TLXQ4d9waJUOKpxORvgX3T56qoVfYTbUkVxe-5VchX000JWy8GdhpyWawldG0au03GhU7jhVnQeMlb7WWaNFOGXQwx6wvF0B30UL-6wgZO1nWD7InaQXJiFZazE0HK0DX61DUbP6PFYJKOBkbSWg9vSSzCeTxVFx9uJXMSAg9_vacpAYmq02ixV8e73CU9_hHhiCqEOYGunzwO4mEm6mn3fhPz6Q5azzPhZTc-lROKc66bmo_Y7jtMRmwgwBTq4diY9bIhw0x_qMsLLEdk1qk-dH9_FhBcunW2PkM8rCyGyDQ2slL-Axs_zg.HgP2g-UNsljcDhO74OMFWKxkYiSo4mbK";

        const entityId = "https://spring.io/security/saml-sp";
        let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
        url += `?providerId=${entityId}&token=${token}`;

        await cas.log(`Navigating to ${url}`);
        await cas.goto(page, url);
        await cas.sleep(5000);

        await cas.logPage(page);
        await cas.assertPageUrl(page, "https://localhost:9876/sp/");
        await cas.assertInnerText(page, "#principal", "casuser");

        await cas.gotoLogin(page);
        await cas.assertCookie(page);
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
        await cas.sleep(1000);
        
        await cas.closeBrowser(browser);
        await cleanUp();
    }, async (error) => {
        await cas.log(error);
        throw error;
    });
})();

