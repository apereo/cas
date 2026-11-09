const assert = require("assert");
const path = require("path");
const {processors} = require("xml2js");
const cas = require("../../cas.js");

const UNSOLICITED_SSO_URL = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
const ACS_URL = "https://localhost:9859/post";
const STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";

function textOf(element) {
    return typeof element === "string" ? element : element._;
}

async function startUnsolicitedSso(page, entityId) {
    const url = `${UNSOLICITED_SSO_URL}?providerId=${encodeURIComponent(entityId)}`;
    const response = await cas.goto(page, url);
    await cas.sleep(2000);
    await cas.logPage(page);
    return response;
}

/*
 * Metadata that fails a filter (signature trust, required signed root, certificate expiry) is not loaded at all,
 * so the service provider has no metadata and unsolicited SSO is refused before any login.
 */
async function assertMetadataRefused(page, entityId, reason) {
    await cas.logg(`${entityId} must be refused: ${reason}`);
    const response = await startUnsolicitedSso(page, entityId);
    assert(response.status() >= 400, `Unsolicited SSO for ${entityId} must be refused, but got ${response.status()}`);
    assert(!page.url().startsWith(ACS_URL), `No response may reach the service provider, but page is at ${page.url()}`);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
}

async function assertMetadataTrusted(page, entityId, reason) {
    await cas.logg(`${entityId} must be accepted: ${reason}`);
    await startUnsolicitedSso(page, entityId);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(3000);
        await cas.logPage(page);
    }
    assert(page.url().startsWith(ACS_URL), `Response for ${entityId} must be posted to ${ACS_URL}, but page is at ${page.url()}`);
    const content = JSON.parse(await cas.innerText(page, "body"));
    assert(content.form.SAMLResponse !== undefined);
    const xml = await cas.base64Decode(content.form.SAMLResponse);
    const parsed = await cas.parseXML(xml, {tagNameProcessors: [processors.stripPrefix]});
    const response = parsed.Response;
    assert(response.Status[0].StatusCode[0].$.Value === STATUS_SUCCESS);
    assert(response.$.Destination === ACS_URL);
    const audience = textOf(response.Assertion[0].Conditions[0].AudienceRestriction[0].Audience[0]);
    assert(audience === entityId, `Assertion must be issued to ${entityId}, but its audience is ${audience}`);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    let success = false;
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLogout(page);

        await assertMetadataRefused(page, "https://localhost:9859/sig-wrong-cert",
            "the aggregate is signed, but not by the certificate the service trusts");
        await assertMetadataRefused(page, "https://localhost:9859/sig-tampered",
            "one byte of the aggregate was changed after it was signed");
        await assertMetadataRefused(page, "https://localhost:9859/sig-unsigned",
            "the aggregate is not signed and the service requires a signed root");
        await assertMetadataRefused(page, "https://localhost:9859/sig-expired-cert",
            "the certificate in the service provider metadata has expired and metadata certificates are validated");

        await assertMetadataTrusted(page, "https://localhost:9859/sig-trusted",
            "the aggregate is signed by the certificate the service trusts");
        await assertMetadataTrusted(page, "https://localhost:9859/sig-unsigned-optional",
            "the aggregate is not signed and the service does not require a signed root");
        await assertMetadataTrusted(page, "https://localhost:9859/sig-expired-cert-unchecked",
            "the certificate in the metadata has expired, but metadata certificates are not validated");
        success = true;
    } finally {
        if (success) {
            await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
        }
        await cas.closeBrowser(browser);
    }
})();
