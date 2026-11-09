const assert = require("assert");
const path = require("path");
const zlib = require("zlib");
const crypto = require("crypto");
const {processors} = require("xml2js");
const cas = require("../../cas.js");

const IDP_ENTITY_ID = "https://cas.apereo.org/saml/idp";
const IDP_SSO_URL = "https://localhost:8443/cas/idp/profile/SAML2/Redirect/SSO";
const UNSOLICITED_SSO_URL = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
const ACS_URL = "https://localhost:9859/post";
const STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";
const PERSISTENT = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";
const EMAIL = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";
const PERSISTENT_ID_SALT = "Q0FTLXBlcnNpc3RlbnQtaWQtc2FsdA";

const SP_PERSISTENT_REQUIRED = "https://localhost:9859/nameid-persistent-a";
const SP_PERSISTENT_FROM_METADATA = "https://localhost:9859/nameid-persistent-c";
const SP_EMAIL_QUALIFIED = "https://localhost:9859/nameid-email-qualified";
const SP_EMAIL_UNQUALIFIED = "https://localhost:9859/nameid-email-unqualified";

/*
 * ShibbolethCompatiblePersistentIdGenerator: base64(SHA-1(entityID "!" principal "!" salt)), where the entity id is the
 * service provider's, so the identifier is stable for one service provider and differs between service providers.
 */
function expectedPersistentId(entityId) {
    return crypto.createHash("sha1")
        .update(entityId).update("!").update("casuser").update("!").update(PERSISTENT_ID_SALT)
        .digest("base64");
}

function textOf(element) {
    return typeof element === "string" ? element : element._;
}

async function loginIfNeeded(page) {
    await cas.sleep(2000);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(3000);
    }
    await cas.logPage(page);
}

async function readNameId(page, entityId, requestId = undefined) {
    assert(page.url().startsWith(ACS_URL), `Response for ${entityId} must be posted to ${ACS_URL}, but page is at ${page.url()}`);
    const content = JSON.parse(await cas.innerText(page, "body"));
    assert(content.form.SAMLResponse !== undefined);
    const xml = await cas.base64Decode(content.form.SAMLResponse);
    await cas.log(xml);
    const response = (await cas.parseXML(xml, {tagNameProcessors: [processors.stripPrefix]})).Response;
    assert(response.Status[0].StatusCode[0].$.Value === STATUS_SUCCESS);
    assert(response.$.InResponseTo === requestId, `InResponseTo must be ${requestId}, but is ${response.$.InResponseTo}`);
    const assertion = response.Assertion[0];
    assert(textOf(assertion.Conditions[0].AudienceRestriction[0].Audience[0]) === entityId);
    const nameId = assertion.Subject[0].NameID[0];
    const result = {
        value: textOf(nameId),
        format: nameId.$.Format,
        nameQualifier: nameId.$.NameQualifier,
        spNameQualifier: nameId.$.SPNameQualifier
    };
    await cas.log(`NameID for ${entityId}: ${JSON.stringify(result)}`);
    return result;
}

async function unsolicitedSso(page, entityId) {
    const url = `${UNSOLICITED_SSO_URL}?providerId=${encodeURIComponent(entityId)}`;
    await cas.goto(page, url);
    await loginIfNeeded(page);
    return readNameId(page, entityId);
}

async function authnRequestWithNameIdPolicy(page, entityId, format) {
    const requestId = `_${crypto.randomUUID()}`;
    const xml = "<samlp:AuthnRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" "
        + "xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" "
        + `ID="${requestId}" Version="2.0" IssueInstant="${new Date().toISOString()}" Destination="${IDP_SSO_URL}" `
        + "ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\">"
        + `<saml:Issuer>${entityId}</saml:Issuer>`
        + `<samlp:NameIDPolicy AllowCreate="true" Format="${format}"/></samlp:AuthnRequest>`;
    const samlRequest = zlib.deflateRawSync(Buffer.from(xml, "utf8")).toString("base64");
    await cas.goto(page, `${IDP_SSO_URL}?SAMLRequest=${encodeURIComponent(samlRequest)}`);
    await loginIfNeeded(page);
    return readNameId(page, entityId, requestId);
}

function assertPersistentNameId(nameId, entityId) {
    assert(nameId.format === PERSISTENT, `NameID format for ${entityId} must be persistent, but is ${nameId.format}`);
    assert(nameId.value === expectedPersistentId(entityId),
        `Persistent NameID for ${entityId} must be ${expectedPersistentId(entityId)}, but is ${nameId.value}`);
    assert(nameId.nameQualifier === IDP_ENTITY_ID, `NameQualifier must default to the IdP entity id, but is ${nameId.nameQualifier}`);
    assert(nameId.spNameQualifier === entityId, `SPNameQualifier must default to ${entityId}, but is ${nameId.spNameQualifier}`);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    let success = false;
    try {
        let context = await browser.createBrowserContext();
        let page = await cas.newPage(context);

        await cas.logg("The service requires the persistent format and derives the value with the Shibboleth-compatible generator");
        const firstLogin = await unsolicitedSso(page, SP_PERSISTENT_REQUIRED);
        assertPersistentNameId(firstLogin, SP_PERSISTENT_REQUIRED);

        await cas.logg("A service without a required format takes it from metadata; the AuthnRequest asks for persistent via NameIDPolicy");
        const otherServiceProvider = await authnRequestWithNameIdPolicy(page, SP_PERSISTENT_FROM_METADATA, PERSISTENT);
        assertPersistentNameId(otherServiceProvider, SP_PERSISTENT_FROM_METADATA);
        assert(otherServiceProvider.value !== firstLogin.value, "The same principal must get a different persistent id at another service provider");

        await cas.logg("Email NameID from the mail attribute, with NameQualifier and SPNameQualifier overridden by the service");
        const qualified = await unsolicitedSso(page, SP_EMAIL_QUALIFIED);
        assert(qualified.format === EMAIL);
        assert(qualified.value === "casuser@example.org");
        assert(qualified.nameQualifier === "https://idp.example.org/name-qualifier", `NameQualifier is ${qualified.nameQualifier}`);
        assert(qualified.spNameQualifier === "https://sp.example.org/sp-name-qualifier", `SPNameQualifier is ${qualified.spNameQualifier}`);
        await context.close();

        await cas.logg("A new login (new browser context, new single sign-on session) yields the same persistent id");
        context = await browser.createBrowserContext();
        page = await cas.newPage(context);
        const secondLogin = await unsolicitedSso(page, SP_PERSISTENT_REQUIRED);
        assertPersistentNameId(secondLogin, SP_PERSISTENT_REQUIRED);
        assert(secondLogin.value === firstLogin.value, "The persistent id must be stable across logins");

        await cas.logg("A qualifier set to none is skipped, as documented for nameIdQualifier and serviceProviderNameIdQualifier");
        const unqualified = await unsolicitedSso(page, SP_EMAIL_UNQUALIFIED);
        assert(unqualified.format === EMAIL);
        assert(unqualified.value === "casuser@example.org");
        assert(unqualified.nameQualifier === undefined, `NameQualifier must be omitted, but is ${unqualified.nameQualifier}`);
        assert(unqualified.spNameQualifier === undefined, `SPNameQualifier must be omitted, but is ${unqualified.spNameQualifier}`);
        await context.close();
        success = true;
    } finally {
        if (success) {
            await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
        }
        await cas.closeBrowser(browser);
    }
})();
