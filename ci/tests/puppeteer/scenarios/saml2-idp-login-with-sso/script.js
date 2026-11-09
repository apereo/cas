
const path = require("path");
const fs = require("fs");
const zlib = require("zlib");
const crypto = require("crypto");
const assert = require("assert");
const {processors} = require("xml2js");
const cas = require("../../cas.js");

const SP_ENTITY_ID = "http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp";
const SP_ACS_URL = "http://localhost:9443/simplesaml/module.php/saml/sp/saml2-acs.php/default-sp";
const IDP_SSO_URL = "https://localhost:8443/cas/idp/profile/SAML2/Redirect/SSO";

const SIGNED_SP_ENTITY_ID = "https://localhost:9859/authnrequest-signed-sp";
const SIGNED_SP_ACS_URL = "https://localhost:9859/post";
const SIG_ALG = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
const STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";
const STATUS_NO_PASSIVE = "urn:oasis:names:tc:SAML:2.0:status:NoPassive";

/*
 * Builds an HTTP-Redirect AuthnRequest. When a key is given, the request carries the
 * binding signature over "SAMLRequest=...&SigAlg=..." as sent (SAML bindings, section 3.4.4.1).
 */
function buildAuthnRequestUrl(requestId, issuer, isPassive, privateKey = undefined) {
    const xml = "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
        + "xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" "
        + `ID="${requestId}" Version="2.0" IssueInstant="${new Date().toISOString()}" `
        + `Destination="${IDP_SSO_URL}" IsPassive="${isPassive}" `
        + "ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" "
        + `AssertionConsumerServiceURL="${SIGNED_SP_ACS_URL}">`
        + `<saml:Issuer>${issuer}</saml:Issuer></samlp:AuthnRequest>`;
    const samlRequest = zlib.deflateRawSync(Buffer.from(xml, "utf8")).toString("base64");
    let query = `SAMLRequest=${encodeURIComponent(samlRequest)}`;
    if (privateKey !== undefined) {
        query += `&SigAlg=${encodeURIComponent(SIG_ALG)}`;
        const signature = crypto.sign("sha256", Buffer.from(query, "utf8"), privateKey).toString("base64");
        query += `&Signature=${encodeURIComponent(signature)}`;
    }
    return `${IDP_SSO_URL}?${query}`;
}

function collectStatusCodes(statusCodes, values = []) {
    for (const statusCode of statusCodes ?? []) {
        values.push(statusCode.$.Value);
        collectStatusCodes(statusCode.StatusCode, values);
    }
    return values;
}

async function readSamlResponseFromAcs(page) {
    assert(page.url().startsWith(SIGNED_SP_ACS_URL), `Response must be posted to ${SIGNED_SP_ACS_URL}, but page is at ${page.url()}`);
    const content = JSON.parse(await cas.innerText(page, "body"));
    assert(content.form.SAMLResponse !== undefined);
    const xml = await cas.base64Decode(content.form.SAMLResponse);
    await cas.log(xml);
    const parsed = await cas.parseXML(xml, {tagNameProcessors: [processors.stripPrefix]});
    return parsed.Response;
}

async function assertAuthnRequestRefused(page, url) {
    const response = await cas.goto(page, url);
    await cas.sleep(1000);
    await cas.logPage(page);
    assert(response.status() === 400, `Authentication request must be refused, but status is ${response.status()}`);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
}

async function verifyAuthnRequestValidation(page) {
    const privateKey = fs.readFileSync(path.join(__dirname, "sp-signing.key"), "utf8");

    await cas.log("Removing any single sign-on session left behind by an earlier attempt");
    await cas.gotoLogout(page);

    await cas.log("A signed passive request without a single sign-on session yields NoPassive");
    const passiveRequestId = `_${crypto.randomUUID()}`;
    await cas.goto(page, buildAuthnRequestUrl(passiveRequestId, SIGNED_SP_ENTITY_ID, true, privateKey));
    await cas.sleep(3000);
    await cas.logPage(page);
    const samlResponse = await readSamlResponseFromAcs(page);
    assert(samlResponse.$.InResponseTo === passiveRequestId);
    const statusCodes = collectStatusCodes(samlResponse.Status[0].StatusCode);
    await cas.log(`Status codes: ${statusCodes}`);
    assert(statusCodes.includes(STATUS_NO_PASSIVE), `Expected ${STATUS_NO_PASSIVE} but got ${statusCodes}`);
    assert(samlResponse.Assertion === undefined);
    assert(samlResponse.EncryptedAssertion === undefined);

    await cas.log("The SP metadata requires signed requests, so an unsigned request is refused");
    await assertAuthnRequestRefused(page, buildAuthnRequestUrl(`_${crypto.randomUUID()}`, SIGNED_SP_ENTITY_ID, false));

    await cas.log("A request signed with a key that is not in the SP metadata is refused");
    const {privateKey: unknownKey} = crypto.generateKeyPairSync("rsa", {modulusLength: 2048});
    await assertAuthnRequestRefused(page, buildAuthnRequestUrl(`_${crypto.randomUUID()}`, SIGNED_SP_ENTITY_ID, false, unknownKey));

    await cas.log("A request from an issuer that is not registered is refused");
    await assertAuthnRequestRefused(page, buildAuthnRequestUrl(`_${crypto.randomUUID()}`, "https://localhost:9859/unknown-sp", false));

    await cas.log("A service with skipValidatingAuthnRequest accepts an unsigned request although its metadata requires signing");
    await cas.goto(page, buildAuthnRequestUrl(`_${crypto.randomUUID()}`, "https://localhost:9859/authnrequest-skip-validation-sp", false));
    await cas.sleep(2000);
    await cas.logPage(page);
    assert(page.url().startsWith("https://localhost:8443/cas/login"), `Request must lead to the login page, but page is at ${page.url()}`);
    await cas.assertVisibility(page, "#username");
    return privateKey;
}

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

    const privateKey = await verifyAuthnRequestValidation(page);
    await cas.separator();

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

    await cas.log("With the SSO session, a signed request from the SP that requires signed requests is answered with Success");
    const signedRequestId = `_${crypto.randomUUID()}`;
    await cas.goto(page, buildAuthnRequestUrl(signedRequestId, SIGNED_SP_ENTITY_ID, false, privateKey));
    await cas.sleep(3000);
    await cas.logPage(page);
    const samlResponse = await readSamlResponseFromAcs(page);
    assert(samlResponse.$.InResponseTo === signedRequestId);
    assert(collectStatusCodes(samlResponse.Status[0].StatusCode)[0] === STATUS_SUCCESS);
    assert(samlResponse.Assertion !== undefined);
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

