
const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");
const os = require("os");
const fs = require("fs");

(async () => {
    let success = false;
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        const service = "https://localhost:9859/anything/cas";
        await cas.gotoLogin(page, service);
        await cas.sleep(1000);
        await cas.loginWith(page);
        await cas.sleep(4000);
        const ticket = await cas.assertTicketParameter(page);
        const body = await cas.doRequest(`https://localhost:8443/cas/validate?service=${service}&ticket=${ticket}`);
        assert(body === "yes\ncasuser\n");

        await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
        await cas.sleep(6000);

        const requestId = `_${await cas.uuid()}`;
        const nameId = await cas.uuid();
        const issueInstant = new Date().toISOString();
        const requestXml = `<?xml version="1.0" encoding="UTF-8"?>
<saml2p:LogoutRequest Destination="https://localhost:8443/cas/idp/profile/SAML2/POST/SLO"
    ID="${requestId}" IssueInstant="${issueInstant}" Version="2.0"
    xmlns:saml2p="urn:oasis:names:tc:SAML:2.0:protocol">
    <saml2:Issuer xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp</saml2:Issuer>
    <saml2:NameID xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">${nameId}</saml2:NameID>
</saml2p:LogoutRequest>`;
        const SAMLRequest = Buffer.from(requestXml, "utf8").toString("base64");
        const sloPage = `
        <html>
        <body onLoad="document.forms[0].submit()">
        <form action="https://localhost:8443/cas/idp/profile/SAML2/POST/SLO" method="post">
            <input type="hidden" name="RelayState" value="36ece039-7a02-42af-90b1-553a3de4f27c"/>
            <input type="hidden" name="SAMLRequest" value="${SAMLRequest}"/>
        </form>
        </body>
        </html>`.trim();
        await cas.log(sloPage);

        const tempDir = os.tmpdir();
        const sloFile = `${tempDir}/saml2slo.html`;

        await fs.writeFileSync(sloFile, sloPage);
        await cas.log(`Logout page is written to ${sloFile}`);

        await cas.goto(page, `file://${sloFile}`);
        await cas.sleep(6000);
        await cas.logPage(page);
        await cas.screenshot(page);
        await cas.assertInnerText(page, "#main-content h2", "Logout successful");
        await cas.assertVisibility(page, "#logoutRedirectButton");
        await cas.click(page, "#logoutRedirectButton");
        success = true;
    } finally {
        if (success) {
            await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
        }
        await cas.closeBrowser(browser);
    }
})();
