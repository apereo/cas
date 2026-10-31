
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

        const token = "eyJhbGciOiJkaXIiLCJlbmMiOiJBMTkyQ0JDLUhTMzg0IiwiY3R5IjoiSldUIn0..6q-WnspGZkzHAd_4LDbRnA.Crc5YCWpZ26j9iJ61RR0t7L_A1NtEbTe-IaqqBrEZBlLFxGIcchMGC6sPXOasewh3GwkukrHGyg5pfXU8OvPHWPvC8riG5T1rEYkfgxVoG2rUOIFrLYHGpLCRdsXRP3EYnE4nnIePlDTmKLVC80ovtUoonow2YVwMK6mv1mqWE-RN-XSv_gav4aroaNvY4AA19Q_ppBYc_VVUJmz1t0pr1BTIRao8Hm56LQU8_kSBDfEuB96wTPpuTafZ3zMshgkLM--Ga-FGLL2Tyx_toMdaQ.QtUB4sH8R0BQ0YsB8QmC-ccAtgwGhLZu";

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

