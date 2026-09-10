
const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    let success = false;
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        const page2 = await cas.newPage(browser);
        const requestId = `_${await cas.uuid()}`;
        const nameId = await cas.uuid();
        const requestXml = `<?xml version="1.0" encoding="UTF-8"?>
<saml2p:LogoutRequest Destination="https://localhost:8443/cas/idp/profile/SAML2/POST/SLO"
    ID="${requestId}" IssueInstant="${new Date().toISOString()}" Version="2.0"
    xmlns:saml2p="urn:oasis:names:tc:SAML:2.0:protocol">
    <saml2:Issuer xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">https://samltest.id/saml/sp</saml2:Issuer>
    <saml2:NameID xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion"
        Format="urn:oasis:names:tc:SAML:2.0:nameid-format:transient">${nameId}</saml2:NameID>
    <saml2p:SessionIndex>${await cas.uuid()}</saml2p:SessionIndex>
</saml2p:LogoutRequest>`;
        const postData = new URLSearchParams({
            SAMLRequest: Buffer.from(requestXml, "utf8").toString("base64")
        }).toString();
        await page2.setRequestInterception(true);
        page2.on("request", (request) => {
            const data = {
                "method": "POST",
                "postData": postData,
                "headers": {
                    ...request.headers(),
                    "Content-Type": "application/x-www-form-urlencoded"
                }
            };
            request.continue(data);
        });

        await cas.goto(page2, "https://localhost:8443/cas/idp/profile/SAML2/POST/SLO");
        await cas.sleep(3000);
        await cas.log("Checking for page URL...");
        await cas.logPage(page2);
        assert(await page2.url() === "https://localhost:9859/post");
        success = true;
    } finally {
        if (success) {
            await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
        }
        await cas.closeBrowser(browser);
    }
})();
