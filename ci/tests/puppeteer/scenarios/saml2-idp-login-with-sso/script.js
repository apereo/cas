
const path = require("path");
const zlib = require("zlib");
const crypto = require("crypto");
const assert = require("assert");
const cas = require("../../cas.js");

const SP_ENTITY_ID = "http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp";
const SP_ACS_URL = "http://localhost:9443/simplesaml/module.php/saml/sp/saml2-acs.php/default-sp";
const IDP_SSO_URL = "https://localhost:8443/cas/idp/profile/SAML2/Redirect/SSO";

async function sendAuthnRequest(page, forceAuthn) {
    const xml = "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
        + "xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" "
        + `ID="_${crypto.randomUUID().replaceAll("-", "")}" Version="2.0" IssueInstant="${new Date().toISOString()}" `
        + `Destination="${IDP_SSO_URL}" ForceAuthn="${forceAuthn}" `
        + "ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" "
        + `AssertionConsumerServiceURL="${SP_ACS_URL}">`
        + `<saml:Issuer>${SP_ENTITY_ID}</saml:Issuer></samlp:AuthnRequest>`;
    const samlRequest = zlib.deflateRawSync(Buffer.from(xml, "utf8")).toString("base64");
    await cas.log(`Sending authentication request with ForceAuthn=${forceAuthn}`);
    await cas.goto(page, `${IDP_SSO_URL}?SAMLRequest=${encodeURIComponent(samlRequest)}`);
    await cas.sleep(3000);
    await cas.logPage(page);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.log("Establishing SSO session...");
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    
    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.sleep(3000);
    await cas.screenshot(page);
    await page.waitForSelector("#table_with_attributes", {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");

    const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);

    await cas.separator();
    await cas.log("Without ForceAuthn, the existing SSO session answers the authentication request");
    await sendAuthnRequest(page, false);
    assert(page.url().startsWith("http://localhost:9443/"), `Response must be delivered to the SP, but page is at ${page.url()}`);

    await cas.separator();
    await cas.log("With ForceAuthn, the user must log in again despite the SSO session");
    await sendAuthnRequest(page, true);
    assert(page.url().startsWith("https://localhost:8443/cas/login"), `ForceAuthn must ask for credentials, but page is at ${page.url()}`);
    await cas.assertVisibility(page, "#username");
    await cas.loginWith(page);
    await cas.sleep(3000);
    await cas.logPage(page);
    assert(page.url().startsWith("http://localhost:9443/"), `Response must be delivered to the SP, but page is at ${page.url()}`);
    await cas.separator();

    const entityId = "https://localhost:9859/shibboleth";
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";
    await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();

