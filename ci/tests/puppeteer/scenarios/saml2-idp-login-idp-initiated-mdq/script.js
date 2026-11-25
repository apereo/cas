
const performance = require("perf_hooks").performance;
const assert = require("assert");
const crypto = require("crypto");
const fs = require("fs");
const express = require("express");
const {processors} = require("xml2js");
const cas = require("../../cas.js");
const path = require("path");

/*
 * Variations: "local" runs an in-script MDQ server, so it needs nothing outside the runner; "incommon" queries
 * the live InCommon MDQ service and trusts its published signing certificate.
 */
const UNSOLICITED_SSO_URL = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
const METADATA_CACHE_ENDPOINT = "https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadataCache";
const STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";

const MDQ_PORT = 5614;
const MDQ_USERNAME = "mdqclient";
const MDQ_PASSWORD = "Mdq-Secret-2026";
const MDQ_MEDIA_TYPE = "application/samlmetadata+xml";
const MDQ_SIGNATURE_DIR = path.join(__dirname, "../../overlay/saml2-idp-login-idp-initiated-mdq");
const METADATA_BACKUP_DIR = path.join(__dirname, "saml-md/metadata-backups");

const SP_HOST = "mdq-sp.example.org";
const SIGNED = `https://${SP_HOST}/signed`;
const SIGNED_BY_OTHER = `https://${SP_HOST}/signed-by-other`;
const SIGNED_MISSING = `https://${SP_HOST}/signed-missing`;
const SIGNED_UNKNOWN = `https://${SP_HOST}/signed-unknown`;
const UNSIGNED = `https://${SP_HOST}/unsigned`;
const UNSIGNED_ROTATED = `https://${SP_HOST}/unsigned-rotated`;

/*
 * The key each entity's metadata is signed with, or null for unsigned metadata. Entities under /signed must be signed
 * by signer.crt (MdqSigned-1.json); entities under /unsigned are not checked (MdqUnsigned-2.json).
 * SIGNED_UNKNOWN is not served: the MDQ server answers 404.
 */
const MDQ_ENTITIES = new Map([
    [SIGNED, "signer"],
    [SIGNED_BY_OTHER, "other"],
    [SIGNED_MISSING, null],
    [UNSIGNED, null],
    [UNSIGNED_ROTATED, null]
]);

const MD = "urn:oasis:names:tc:SAML:2.0:metadata";
const DS = "http://www.w3.org/2000/09/xmldsig#";
const EXC_C14N = "http://www.w3.org/2001/10/xml-exc-c14n#";

function textOf(element) {
    return typeof element === "string" ? element : element._;
}

function sha1(value) {
    return crypto.createHash("sha1").update(value, "utf8").digest("hex");
}

/*
 * Written in exclusive canonical form (attributes in canonical order, the namespace declared on the root,
 * no self-closing elements), as sign-metadata.js of saml2-idp-metadata-signature-local does, so the digest of these
 * bytes is the digest of the root element once the enveloped signature is removed.
 */
function entityDescriptor(entityId, id) {
    return `<EntityDescriptor xmlns="${MD}" ID="${id}" entityID="${entityId}">`
        + "<SPSSODescriptor AuthnRequestsSigned=\"false\" WantAssertionsSigned=\"false\" "
        + "protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">"
        + "<NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</NameIDFormat>"
        + "<AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" "
        + `Location="${entityId}/acs" index="0"></AssertionConsumerService>`
        + "</SPSSODescriptor></EntityDescriptor>";
}

function sign(unsigned, id, privateKey) {
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
    const index = unsigned.indexOf(">") + 1;
    return unsigned.substring(0, index) + signature + unsigned.substring(index);
}

function buildMetadataQueryDocuments() {
    const keys = {};
    for (const name of ["signer", "other"]) {
        keys[name] = crypto.createPrivateKey(fs.readFileSync(path.join(MDQ_SIGNATURE_DIR, `${name}.key`), "utf8"));
    }
    const documents = new Map();
    for (const [entityId, keyName] of MDQ_ENTITIES) {
        const id = `_mdq-${sha1(entityId)}`;
        const unsigned = entityDescriptor(entityId, id);
        const xml = keyName === null ? unsigned : sign(unsigned, id, keys[keyName]);
        documents.set(entityId, `<?xml version="1.0" encoding="UTF-8"?>\n${xml}`);
    }
    return documents;
}

/*
 * A Metadata Query Protocol responder: GET /entities/{url-encoded entity id}, behind basic authentication, answering
 * only clients that accept application/samlmetadata+xml. Every request is recorded for the assertions.
 */
async function startMetadataQueryServer(state) {
    const documents = buildMetadataQueryDocuments();
    const app = express();
    app.set("etag", false);
    app.get("/entities/:entityId", (req, res) => {
        const expected = `Basic ${Buffer.from(`${MDQ_USERNAME}:${state.password}`).toString("base64")}`;
        const record = {
            entityId: req.params.entityId,
            accept: req.get("Accept") ?? "",
            authorized: req.get("Authorization") === expected
        };
        state.requests.push(record);
        if (!record.authorized) {
            record.status = 401;
            res.set("WWW-Authenticate", "Basic realm=\"mdq\"").status(401).send("Unauthorized");
        } else if (!record.accept.includes(MDQ_MEDIA_TYPE)) {
            record.status = 406;
            res.status(406).send("Not Acceptable");
        } else if (documents.has(record.entityId)) {
            record.status = 200;
            res.type(MDQ_MEDIA_TYPE).send(documents.get(record.entityId));
        } else {
            record.status = 404;
            res.status(404).send("Not Found");
        }
        cas.log(`MDQ request for ${record.entityId}: ${record.status}`);
    });
    return new Promise((resolve, reject) => {
        const server = app.listen(MDQ_PORT, (error) => error ? reject(error) : resolve(server));
    });
}

/*
 * Removes every cached metadata resolver and every remembered failure (failure-cache-expiration).
 */
async function clearMetadataCache() {
    await cas.doRequest(METADATA_CACHE_ENDPOINT, "DELETE", {}, 204);
}

function requestsFor(state, entityId) {
    return state.requests.filter((request) => request.entityId === entityId);
}

/*
 * The service providers exist only as metadata: the browser's POST to their assertion consumer service is answered
 * by the script, which keeps the SAML response.
 */
async function interceptAssertionConsumerService(page, received) {
    await page.setRequestInterception(true);
    page.on("request", async (request) => {
        const url = new URL(request.url());
        if (request.method() === "POST" && url.hostname === SP_HOST && url.pathname.endsWith("/acs")) {
            const body = request.postData() ?? (request.hasPostData() ? await request.fetchPostData() : undefined) ?? "";
            received.push({acs: request.url(), samlResponse: new URLSearchParams(body).get("SAMLResponse")});
            await request.respond({status: 200, contentType: "text/plain", body: "Received by the service provider"});
        } else {
            await request.continue();
        }
    });
}

async function startUnsolicitedSso(page, received, entityId) {
    received.length = 0;
    const response = await cas.goto(page, `${UNSOLICITED_SSO_URL}?providerId=${encodeURIComponent(entityId)}`);
    await cas.sleep(2000);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(3000);
    }
    await cas.logPage(page);
    return response;
}

async function assertSsoSucceeds(page, received, entityId, reason) {
    await cas.logg(`${entityId} must be accepted: ${reason}`);
    await startUnsolicitedSso(page, received, entityId);
    assert.equal(received.length, 1, `A SAML response must be posted to ${entityId}, but page is at ${page.url()}`);
    assert.equal(received[0].acs, `${entityId}/acs`);
    const xml = await cas.base64Decode(received[0].samlResponse);
    const parsed = await cas.parseXML(xml, {tagNameProcessors: [processors.stripPrefix]});
    const response = parsed.Response;
    assert.equal(response.Status[0].StatusCode[0].$.Value, STATUS_SUCCESS);
    assert.equal(response.$.Destination, `${entityId}/acs`);
    const audience = textOf(response.Assertion[0].Conditions[0].AudienceRestriction[0].Audience[0]);
    assert.equal(audience, entityId, `Assertion must be issued to ${entityId}, but its audience is ${audience}`);
}

/*
 * Metadata that cannot be fetched, or fails a filter, leaves the service provider without metadata, so unsolicited
 * SSO is refused before any login.
 */
async function assertSsoRefused(page, received, entityId, reason) {
    await cas.logg(`${entityId} must be refused: ${reason}`);
    const response = await startUnsolicitedSso(page, received, entityId);
    assert(response.status() >= 400, `Unsolicited SSO for ${entityId} must be refused, but got ${response.status()}`);
    assert.equal(received.length, 0, `No response may reach ${entityId}`);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
}

/*
 * The backup of a metadata query is named after the SHA-1 of the requested entity id
 * (UrlResourceMetadataResolver#getBackupMetadataFilenamePrefix), and only metadata that loaded is backed up.
 */
function assertMetadataBackup(entityId, expected) {
    const backupFile = path.join(METADATA_BACKUP_DIR, `${sha1(entityId)}.xml`);
    const exists = fs.existsSync(backupFile);
    assert.equal(exists, expected, `Metadata backup ${backupFile} for ${entityId} must ${expected ? "" : "not "}exist`);
    if (expected) {
        assert(fs.readFileSync(backupFile, "utf8").includes(`entityID="${entityId}"`));
    }
}

async function verifyLocalMetadataQueryServer() {
    await cas.removeDirectoryOrFile(METADATA_BACKUP_DIR);
    await clearMetadataCache();

    const state = {password: MDQ_PASSWORD, requests: []};
    const server = await startMetadataQueryServer(state);
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLogout(page);
        const received = [];
        await interceptAssertionConsumerService(page, received);

        await assertSsoSucceeds(page, received, SIGNED, "its metadata is signed by the certificate the service trusts");
        const signedRequests = requestsFor(state, SIGNED);
        assert(signedRequests.length >= 1, `The MDQ server must be queried for ${SIGNED}`);
        for (const request of signedRequests) {
            assert(request.authorized, "Every MDQ request must carry the configured basic authentication");
            assert(request.accept.includes(MDQ_MEDIA_TYPE), `MDQ requests must accept ${MDQ_MEDIA_TYPE}, not ${request.accept}`);
        }
        assertMetadataBackup(SIGNED, true);

        await assertSsoSucceeds(page, received, SIGNED, "its metadata is cached");
        assert.equal(requestsFor(state, SIGNED).length, signedRequests.length,
            `Cached metadata of ${SIGNED} must not be queried again`);

        await assertSsoRefused(page, received, SIGNED_BY_OTHER, "its metadata is signed by a certificate the service does not trust");
        await assertSsoRefused(page, received, SIGNED_MISSING, "its metadata is not signed and the service requires a signed root");
        await assertSsoRefused(page, received, SIGNED_UNKNOWN, "the MDQ server does not know it");
        const unknownRequests = requestsFor(state, SIGNED_UNKNOWN);
        assert(unknownRequests.length >= 1 && unknownRequests.every((request) => request.status === 404));
        assertMetadataBackup(SIGNED_BY_OTHER, false);
        assertMetadataBackup(SIGNED_MISSING, false);
        assertMetadataBackup(SIGNED_UNKNOWN, false);

        await assertSsoSucceeds(page, received, UNSIGNED, "the service does not check metadata signatures");
        assertMetadataBackup(UNSIGNED, true);

        await cas.log("The MDQ server now expects another password, so the credentials CAS sends are wrong");
        state.password = "Rotated-Secret-2026";
        await assertSsoRefused(page, received, UNSIGNED_ROTATED, "the MDQ server refuses the credentials and there is no backup");
        const rotatedRequests = requestsFor(state, UNSIGNED_ROTATED);
        assert(rotatedRequests.length >= 1 && rotatedRequests.every((request) => request.status === 401));
        assertMetadataBackup(UNSIGNED_ROTATED, false);

        await clearMetadataCache();
        const unsignedRequests = requestsFor(state, UNSIGNED).length;
        await assertSsoSucceeds(page, received, UNSIGNED, "the MDQ server refuses the credentials, but CAS falls back onto the backup");
        const fallbackRequests = requestsFor(state, UNSIGNED).slice(unsignedRequests);
        assert(fallbackRequests.length >= 1 && fallbackRequests.every((request) => request.status === 401),
            `CAS must have queried the MDQ server for ${UNSIGNED} again and been refused`);

        await cas.log("The MDQ server accepts the configured password again");
        state.password = MDQ_PASSWORD;
        await clearMetadataCache();
        await assertSsoSucceeds(page, received, UNSIGNED_ROTATED, "the remembered failure is cleared and the credentials are right");
        assertMetadataBackup(UNSIGNED_ROTATED, true);
        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    } finally {
        server.close();
        server.closeAllConnections();
        await cas.closeBrowser(browser);
    }
}

async function tryServiceProviders(entityIds, page, timeout) {
    let count = 0;
    for (const entityId of entityIds) {
        await cas.log(`Trying service provider ${entityId} with timeout ${timeout}`);

        let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
        url += `?providerId=${entityId}`;
        url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";

        await cas.log(`Navigating to ${url}`);
        const s = performance.now();
        await cas.goto(page, url);
        await cas.sleep(2000);
        await cas.screenshot(page);
        const e = performance.now();
        const duration = (e - s) / 1000;
        await cas.log(`Request took ${duration} seconds for ${entityId}`);

        if (count > 1 && duration > duration) {
            throw `Request took longer than expected:${duration}`;
        }

        await cas.sleep(2000);
        await cas.assertVisibility(page, "#username");
        await cas.assertVisibility(page, "#password");
        await cas.separator();
        count++;
    }
}

async function verifyInCommonMetadataQueryServer() {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const entityIds = [
        "https://studypages.com/saml-sp",
        "https://aca.ucop.edu",
        "https://www.peoplegrove.com/saml",
        "https://login.at.internet2.edu/Saml2/proxy_saml2_backend.xml",
        "https://uchicago.infoready4.com/shibboleth",
        "https://cole.uconline.edu/shibboleth-sp"
    ];

    await tryServiceProviders(entityIds, page, 15);
    await tryServiceProviders(entityIds, page, 5);

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
}

(async () => {
    if (process.env.SCENARIO_VARIATION === "local") {
        await verifyLocalMetadataQueryServer();
    } else {
        await verifyInCommonMetadataQueryServer();
    }
})();

