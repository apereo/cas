const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");
const fs = require("fs");
const zlib = require("zlib");
const crypto = require("crypto");
const {processors} = require("xml2js");

const SP_ENTITY_ID = "https://localhost:9859/soap-sp";
const SP_ARTIFACT_ACS_URL = "https://localhost:9859/anything/soap-sp/artifact";
const IDP_SSO_URL = "https://localhost:8443/cas/idp/profile/SAML2/Redirect/SSO";
const ARTIFACT_RESOLUTION_URL = "https://localhost:8443/cas/idp/profile/SAML1/SOAP/ArtifactResolution";
const ATTRIBUTE_QUERY_URL = "https://localhost:8443/cas/idp/profile/SAML2/SOAP/AttributeQuery";
const ECP_URL = "https://localhost:8443/cas/idp/profile/SAML2/SOAP/ECP";

const SAMLP = "urn:oasis:names:tc:SAML:2.0:protocol";
const SAML = "urn:oasis:names:tc:SAML:2.0:assertion";
const DS = "http://www.w3.org/2000/09/xmldsig#";
const EXC_C14N = "http://www.w3.org/2001/10/xml-exc-c14n#";
const STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";

function escapeText(value) {
    return String(value).replaceAll("&", "&amp;").replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;").replaceAll("\r", "&#xD;");
}

function escapeAttribute(value) {
    return String(value).replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;")
        .replaceAll("\t", "&#x9;").replaceAll("\n", "&#xA;").replaceAll("\r", "&#xD;");
}

/*
 * SOAP requests are signed with an enveloped XML signature. The request is written in its exclusive
 * canonical form (no whitespace between elements, namespaces declared where first used, attributes in
 * canonical order), so its digest is the digest of these exact bytes, and the same holds for SignedInfo.
 * The signature goes right after the Issuer, where the RequestAbstractType schema places it.
 */
function signRequest(unsigned, id, privateKey) {
    const digest = crypto.createHash("sha256").update(unsigned, "utf8").digest("base64");
    const signedInfo = `<ds:SignedInfo xmlns:ds="${DS}">`
        + `<ds:CanonicalizationMethod Algorithm="${EXC_C14N}"></ds:CanonicalizationMethod>`
        + "<ds:SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"></ds:SignatureMethod>"
        + `<ds:Reference URI="#${id}"><ds:Transforms>`
        + "<ds:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"></ds:Transform>"
        + `<ds:Transform Algorithm="${EXC_C14N}"></ds:Transform></ds:Transforms>`
        + "<ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"></ds:DigestMethod>"
        + `<ds:DigestValue>${digest}</ds:DigestValue></ds:Reference></ds:SignedInfo>`;
    const signatureValue = crypto.sign("sha256", Buffer.from(signedInfo, "utf8"), privateKey).toString("base64");
    const signature = `<ds:Signature xmlns:ds="${DS}">${signedInfo}<ds:SignatureValue>${signatureValue}</ds:SignatureValue></ds:Signature>`;
    const marker = "</saml:Issuer>";
    const index = unsigned.indexOf(marker) + marker.length;
    assert(index > marker.length);
    return unsigned.substring(0, index) + signature + unsigned.substring(index);
}

function soapEnvelope(body) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<soap11:Envelope xmlns:soap11=\"http://schemas.xmlsoap.org/soap/envelope/\">"
        + `<soap11:Body>${body}</soap11:Body></soap11:Envelope>`;
}

function newRequestId() {
    return `_${crypto.randomUUID()}`;
}

function requestAttributes(destination, id) {
    return `Destination="${escapeAttribute(destination)}" ID="${id}" IssueInstant="${new Date().toISOString()}" Version="2.0"`;
}

function buildArtifactResolve(id, artifact) {
    return `<samlp:ArtifactResolve xmlns:samlp="${SAMLP}" ${requestAttributes(ARTIFACT_RESOLUTION_URL, id)}>`
        + `<saml:Issuer xmlns:saml="${SAML}">${SP_ENTITY_ID}</saml:Issuer>`
        + `<samlp:Artifact>${escapeText(artifact)}</samlp:Artifact></samlp:ArtifactResolve>`;
}

function buildAttributeQuery(id, nameId) {
    const format = nameId.format === undefined ? "" : ` Format="${escapeAttribute(nameId.format)}"`;
    return `<samlp:AttributeQuery xmlns:samlp="${SAMLP}" ${requestAttributes(ATTRIBUTE_QUERY_URL, id)}>`
        + `<saml:Issuer xmlns:saml="${SAML}">${SP_ENTITY_ID}</saml:Issuer>`
        + `<saml:Subject xmlns:saml="${SAML}"><saml:NameID${format}>${escapeText(nameId.value)}</saml:NameID></saml:Subject>`
        + "</samlp:AttributeQuery>";
}

function buildEcpAuthnRequest(issuer) {
    return `<samlp:AuthnRequest xmlns:samlp="${SAMLP}" xmlns:saml="${SAML}" ID="${newRequestId()}" `
        + `IssueInstant="${new Date().toISOString()}" Version="2.0" `
        + "ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:PAOS\">"
        + `<saml:Issuer>${issuer}</saml:Issuer></samlp:AuthnRequest>`;
}

async function postSoap(url, body, statusCode = 200, headers = {}) {
    const response = await cas.doRequest(url, "POST", {
        "Content-Type": "text/xml",
        "Accept": "text/xml",
        "Content-Length": Buffer.byteLength(body),
        ...headers
    }, statusCode, body);
    await cas.log(response);
    return response;
}

async function parseSoapEnvelope(body) {
    const match = body.match(/<(?:[\w.-]+:)?Envelope[\s>][\s\S]*<\/(?:[\w.-]+:)?Envelope>/);
    assert(match !== null, `No SOAP envelope can be found in ${body}`);
    const parsed = await cas.parseXML(match[0], {tagNameProcessors: [processors.stripPrefix]});
    console.dir(parsed, {depth: null, colors: true});
    return parsed.Envelope;
}

async function assertSoapFault(body) {
    const envelope = await parseSoapEnvelope(body);
    const soapBody = envelope.Body[0];
    assert(soapBody.Fault !== undefined, "SOAP response must carry a fault");
    assert(soapBody.Response === undefined);
    assert(soapBody.ArtifactResponse === undefined);
    await cas.log(`SOAP fault: ${JSON.stringify(soapBody.Fault[0].faultstring)}`);
    return soapBody.Fault[0];
}

function textOf(element) {
    return typeof element === "string" ? element : element._;
}

async function verifyEcpProfile() {
    await cas.log("ECP: a request without basic authentication credentials is answered with 401");
    const envelope = soapEnvelope(buildEcpAuthnRequest(SP_ENTITY_ID));
    await postSoap(ECP_URL, envelope, 401);

    await cas.log("ECP: wrong credentials produce a SOAP fault and no assertion");
    const badCredentials = Buffer.from("casuser:not-the-password").toString("base64");
    let body = await postSoap(ECP_URL, soapEnvelope(buildEcpAuthnRequest(SP_ENTITY_ID)), 200,
        {"Authorization": `Basic ${badCredentials}`});
    await assertSoapFault(body);

    await cas.log("ECP: a request from an unregistered issuer produces a SOAP fault");
    const credentials = Buffer.from("casuser:Mellon").toString("base64");
    body = await postSoap(ECP_URL, soapEnvelope(buildEcpAuthnRequest("https://localhost:9859/unknown-sp")), 200,
        {"Authorization": `Basic ${credentials}`});
    await assertSoapFault(body);
}

async function loginWithArtifactBinding(page) {
    const requestId = newRequestId();
    const xml = `<samlp:AuthnRequest xmlns:samlp="${SAMLP}" xmlns:saml="${SAML}" ID="${requestId}" Version="2.0" `
        + `IssueInstant="${new Date().toISOString()}" Destination="${IDP_SSO_URL}" `
        + "ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Artifact\">"
        + `<saml:Issuer>${SP_ENTITY_ID}</saml:Issuer></samlp:AuthnRequest>`;
    const samlRequest = zlib.deflateRawSync(Buffer.from(xml, "utf8")).toString("base64");
    await cas.goto(page, `${IDP_SSO_URL}?SAMLRequest=${encodeURIComponent(samlRequest)}`);
    await cas.sleep(2000);
    await cas.loginWith(page);
    await cas.sleep(3000);
    await cas.logPage(page);
    assert(page.url().startsWith(SP_ARTIFACT_ACS_URL), `Artifact must be delivered to ${SP_ARTIFACT_ACS_URL}, but page is at ${page.url()}`);
    const content = JSON.parse(await cas.innerText(page, "body"));
    await cas.log(content);
    const parameters = {...content.form, ...content.args};
    assert(typeof parameters.SAMLart === "string" && parameters.SAMLart.length > 0, "The SP must receive an artifact");
    assert(parameters.SAMLResponse === undefined, "The SP must not receive the response itself");
    return {requestId, artifact: parameters.SAMLart};
}

async function verifyArtifactResolution(page, privateKey) {
    const {requestId, artifact} = await loginWithArtifactBinding(page);

    await cas.log("Artifact resolution requires a signed ArtifactResolve; an unsigned one is refused and does not consume the artifact");
    let body = await postSoap(ARTIFACT_RESOLUTION_URL, soapEnvelope(buildArtifactResolve(newRequestId(), artifact)));
    await assertSoapFault(body);

    await cas.log("A signed ArtifactResolve returns the SAML response for the artifact");
    const resolveId = newRequestId();
    body = await postSoap(ARTIFACT_RESOLUTION_URL,
        soapEnvelope(signRequest(buildArtifactResolve(resolveId, artifact), resolveId, privateKey)));
    const envelope = await parseSoapEnvelope(body);
    assert(envelope.Body[0].Fault === undefined, "Artifact resolution must not fail");
    const artifactResponse = envelope.Body[0].ArtifactResponse[0];
    assert(artifactResponse.$.InResponseTo === resolveId);
    assert(artifactResponse.Status[0].StatusCode[0].$.Value === STATUS_SUCCESS);
    const samlResponse = artifactResponse.Response[0];
    assert(samlResponse.$.InResponseTo === requestId);
    assert(samlResponse.Status[0].StatusCode[0].$.Value === STATUS_SUCCESS);
    const assertion = samlResponse.Assertion[0];
    const nameIdElement = assertion.Subject[0].NameID[0];
    const nameId = {
        value: textOf(nameIdElement),
        format: typeof nameIdElement === "string" ? undefined : nameIdElement.$.Format
    };
    await cas.log(`NameID: ${JSON.stringify(nameId)}`);
    assert(nameId.value !== undefined && nameId.value.length > 0);

    await cas.log("Artifacts are single-use: resolving the same artifact again fails");
    const replayId = newRequestId();
    body = await postSoap(ARTIFACT_RESOLUTION_URL,
        soapEnvelope(signRequest(buildArtifactResolve(replayId, artifact), replayId, privateKey)));
    await assertSoapFault(body);
    return nameId;
}

async function verifyAttributeQuery(nameId, privateKey) {
    await cas.log("Attribute queries must be signed");
    let body = await postSoap(ATTRIBUTE_QUERY_URL, soapEnvelope(buildAttributeQuery(newRequestId(), nameId)));
    await assertSoapFault(body);

    /*
     * Only the outcome and the upper bound of the release are asserted: the release policy looks up the
     * service provider metadata by an entity id it takes from the service attributes or request parameters
     * (SamlIdPSAttributeReleasePolicyUtils.getEntityIdFromRequest), and neither is present on a SOAP
     * attribute query, so the policy may release nothing at all. It must never release more than mail and sn.
     */
    await cas.log("A signed attribute query for the NameID of the SSO session releases nothing beyond the allowed attributes");
    const queryId = newRequestId();
    body = await postSoap(ATTRIBUTE_QUERY_URL,
        soapEnvelope(signRequest(buildAttributeQuery(queryId, nameId), queryId, privateKey)));
    const envelope = await parseSoapEnvelope(body);
    assert(envelope.Body[0].Fault === undefined, "Attribute query must not fail");
    const samlResponse = envelope.Body[0].Response[0];
    assert(samlResponse.$.InResponseTo === queryId);
    assert(samlResponse.Status[0].StatusCode[0].$.Value === STATUS_SUCCESS);
    assert(samlResponse.Signature !== undefined, "The response must be signed (signResponses defaults to true)");
    const assertion = samlResponse.Assertion[0];
    assert(textOf(assertion.Subject[0].NameID[0]) === nameId.value);
    const attributes = (assertion.AttributeStatement ?? []).flatMap((statement) => statement.Attribute ?? []);
    const names = attributes.map((attribute) => attribute.$.FriendlyName ?? attribute.$.Name);
    await cas.log(`Released attributes: ${names}`);
    assert(names.every((name) => ["mail", "sn"].includes(name)), `Only mail and sn may be released, but got ${names}`);
    const mail = attributes.find((attribute) => (attribute.$.FriendlyName ?? attribute.$.Name) === "mail");
    if (mail !== undefined) {
        assert(mail.AttributeValue.map(textOf).includes("casuser@example.org"));
    }

    await cas.log("An attribute query for a NameID without an SSO session fails");
    const unknownId = newRequestId();
    const unknownNameId = {value: crypto.randomUUID(), format: nameId.format};
    body = await postSoap(ATTRIBUTE_QUERY_URL,
        soapEnvelope(signRequest(buildAttributeQuery(unknownId, unknownNameId), unknownId, privateKey)));
    await assertSoapFault(body);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    let success = false;
    try {
        const privateKey = fs.readFileSync(path.join(__dirname, "sp-signing.key"), "utf8");
        const page = await cas.newPage(browser);
        await cas.gotoLogout(page);

        await verifyEcpProfile();
        await cas.separator();
        const nameId = await verifyArtifactResolution(page, privateKey);
        await cas.separator();
        await verifyAttributeQuery(nameId, privateKey);
        success = true;
    } finally {
        if (success) {
            await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
        }
        await cas.closeBrowser(browser);
    }
})();
