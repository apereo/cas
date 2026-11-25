const cas = require("../../cas.js");
const assert = require("assert");
const crypto = require("crypto");
const fs = require("fs");
const path = require("path");
const express = require("express");
const {processors} = require("xml2js");

const UNSOLICITED_SSO_URL = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
const METADATA_CACHE_ENDPOINT = "https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadataCache";
const STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";

/*
 * Service providers 3, 4 and 5 read their metadata from this in-script server; their assertion consumer services
 * are answered by the script. cas.authn.saml-idp.metadata.http.metadata-backup-location points CAS at the overlay
 * folder, where it keeps the backups in metadata-backups/, and metadata-backup-max-age is 20 seconds.
 */
const METADATA_PORT = 5615;
const METADATA_BACKUP_DIR = path.join(__dirname, "../../overlay/saml2-idp-metadata-caching/metadata-backups");
const METADATA_BACKUP_MAX_AGE_SECONDS = 20;
const MD = "urn:oasis:names:tc:SAML:2.0:metadata";
const SP_DOMAIN = ".sp.example.org";
const RESILIENT = {id: 3, entityId: "https://resilient.sp.example.org", document: "resilient.xml"};
const NEVER_LOADED = {id: 4, entityId: "https://never-loaded.sp.example.org", document: "never-loaded.xml"};
const EXPIRED = {id: 5, entityId: "https://expired.sp.example.org", document: "expired.xml", validUntil: "2020-01-01T00:00:00Z"};
const MAINTENANCE_PAGE = "<!DOCTYPE html><html><body>Down for maintenance</body></html>";

function textOf(element) {
    return typeof element === "string" ? element : element._;
}

function metadataLocation(sp) {
    return `http://localhost:${METADATA_PORT}/metadata/${sp.document}`;
}

function entityDescriptor(sp) {
    const validUntil = sp.validUntil === undefined ? "" : ` validUntil="${sp.validUntil}"`;
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + `<EntityDescriptor xmlns="${MD}" entityID="${sp.entityId}"${validUntil}>`
        + "<SPSSODescriptor AuthnRequestsSigned=\"false\" WantAssertionsSigned=\"false\" "
        + "protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">"
        + "<NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</NameIDFormat>"
        + "<AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" "
        + `Location="${sp.entityId}/acs" index="0"/>`
        + "</SPSSODescriptor></EntityDescriptor>";
}

/*
 * Serves the metadata of the service providers. state.mode decides the answer: "ok" serves the metadata, "error"
 * answers 500, and "maintenance" answers 200 with an HTML page that is not metadata.
 */
async function startMetadataServer(state) {
    const documents = new Map([RESILIENT, NEVER_LOADED, EXPIRED].map((sp) => [sp.document, entityDescriptor(sp)]));
    const app = express();
    app.set("etag", false);
    app.get("/metadata/:document", (req, res) => {
        const record = {document: req.params.document, mode: state.mode};
        state.requests.push(record);
        if (!documents.has(record.document)) {
            record.status = 404;
            res.status(404).send("Not Found");
        } else if (state.mode === "error") {
            record.status = 500;
            res.status(500).send("Internal Server Error");
        } else if (state.mode === "maintenance") {
            record.status = 200;
            res.type("text/html").send(MAINTENANCE_PAGE);
        } else {
            record.status = 200;
            res.type("application/samlmetadata+xml").send(documents.get(record.document));
        }
        cas.log(`Metadata request for ${record.document} in mode ${record.mode}: ${record.status}`);
    });
    return new Promise((resolve, reject) => {
        const server = app.listen(METADATA_PORT, (error) => error ? reject(error) : resolve(server));
    });
}

function stopMetadataServer(server) {
    server.close();
    server.closeAllConnections();
}

function requestsFor(state, sp) {
    return state.requests.filter((request) => request.document === sp.document);
}

async function clearMetadataCache() {
    await cas.doRequest(METADATA_CACHE_ENDPOINT, "DELETE", {}, 204);
}

async function cacheEntryFor(sp) {
    const body = await cas.doRequest(`${METADATA_CACHE_ENDPOINT}/statistics`, "GET", {"Accept": "application/json"}, 200);
    const entry = JSON.parse(body).entries.find((candidate) => candidate.registeredServiceId === sp.id);
    assert(entry !== undefined, `The metadata cache must describe service ${sp.id}`);
    await cas.log(entry);
    return entry;
}

/*
 * A URL metadata backup is named after the SHA-1 of the metadata location
 * (UrlResourceMetadataResolver#getBackupMetadataFilenamePrefix).
 */
function metadataBackupFile(sp) {
    const name = crypto.createHash("sha1").update(metadataLocation(sp), "utf8").digest("hex");
    return path.join(METADATA_BACKUP_DIR, `${name}.xml`);
}

async function interceptAssertionConsumerService(page, received) {
    await page.setRequestInterception(true);
    page.on("request", async (request) => {
        const url = new URL(request.url());
        if (request.method() === "POST" && url.hostname.endsWith(SP_DOMAIN) && url.pathname === "/acs") {
            const body = request.postData() ?? (request.hasPostData() ? await request.fetchPostData() : undefined) ?? "";
            received.push({acs: request.url(), samlResponse: new URLSearchParams(body).get("SAMLResponse")});
            await request.respond({status: 200, contentType: "text/plain", body: "Received by the service provider"});
        } else {
            await request.continue();
        }
    });
}

async function startUnsolicitedSso(page, received, sp) {
    received.length = 0;
    const response = await cas.goto(page, `${UNSOLICITED_SSO_URL}?providerId=${encodeURIComponent(sp.entityId)}`);
    await cas.sleep(2000);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(3000);
    }
    await cas.logPage(page);
    return response;
}

async function assertSsoSucceeds(page, received, sp, reason) {
    await cas.logg(`${sp.entityId} must be accepted: ${reason}`);
    await startUnsolicitedSso(page, received, sp);
    assert.equal(received.length, 1, `A SAML response must be posted to ${sp.entityId}, but page is at ${page.url()}`);
    const xml = await cas.base64Decode(received[0].samlResponse);
    const parsed = await cas.parseXML(xml, {tagNameProcessors: [processors.stripPrefix]});
    const response = parsed.Response;
    assert.equal(response.Status[0].StatusCode[0].$.Value, STATUS_SUCCESS);
    assert.equal(response.$.Destination, `${sp.entityId}/acs`);
    const audience = textOf(response.Assertion[0].Conditions[0].AudienceRestriction[0].Audience[0]);
    assert.equal(audience, sp.entityId, `Assertion must be issued to ${sp.entityId}, but its audience is ${audience}`);
}

async function assertSsoRefused(page, received, sp, reason) {
    await cas.logg(`${sp.entityId} must be refused: ${reason}`);
    const response = await startUnsolicitedSso(page, received, sp);
    assert(response.status() >= 400, `Unsolicited SSO for ${sp.entityId} must be refused, but got ${response.status()}`);
    assert.equal(received.length, 0, `No response may reach ${sp.entityId}`);
    await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
}

/*
 * The metadata backup is the last known good copy of URL metadata: CAS serves it when the metadata server fails,
 * answers with something that is not metadata, or is gone, and a service provider without a backup is refused.
 */
async function verifyMetadataBackupResilience() {
    await cas.removeDirectoryOrFile(METADATA_BACKUP_DIR);
    await clearMetadataCache();
    const state = {mode: "ok", requests: []};
    let server = await startMetadataServer(state);
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLogout(page);
        const received = [];
        await interceptAssertionConsumerService(page, received);

        await assertSsoSucceeds(page, received, RESILIENT, "its metadata server is up");
        assert(requestsFor(state, RESILIENT).length >= 1);
        const backupFile = metadataBackupFile(RESILIENT);
        assert(fs.existsSync(backupFile), `Metadata backup ${backupFile} must exist`);
        const backup = fs.readFileSync(backupFile, "utf8");
        assert(backup.includes(`entityID="${RESILIENT.entityId}"`), `Metadata backup ${backupFile} must hold the metadata`);
        let entry = await cacheEntryFor(RESILIENT);
        assert.equal(entry.metadataBackupFile, fs.realpathSync(backupFile));
        assert.equal(entry.metadataBackupExists, true);
        assert.equal(entry.metadataBackupStale, false);

        await assertSsoRefused(page, received, EXPIRED, "its metadata expired and require-valid-metadata is on");

        for (const [mode, reason] of [
            ["error", "the metadata server answers 500"],
            ["maintenance", "the metadata server answers 200 with a maintenance page"]
        ]) {
            await cas.log(`Metadata server mode is now ${mode}`);
            state.mode = mode;
            await clearMetadataCache();
            const before = requestsFor(state, RESILIENT).length;
            await assertSsoSucceeds(page, received, RESILIENT, `${reason}, and CAS falls back onto the metadata backup`);
            const attempts = requestsFor(state, RESILIENT).slice(before);
            assert(attempts.length >= 1 && attempts.every((request) => request.mode === mode),
                `CAS must have asked the metadata server again in mode ${mode}`);
            assert.equal(fs.readFileSync(backupFile, "utf8"), backup, `What the server answered in mode ${mode} must not replace the backup`);
        }

        await cas.log("Stopping the metadata server");
        stopMetadataServer(server);
        server = undefined;
        const age = Date.now() - fs.statSync(backupFile).mtimeMs;
        const wait = METADATA_BACKUP_MAX_AGE_SECONDS * 1000 + 2000 - age;
        if (wait > 0) {
            await cas.log(`Waiting ${wait} ms for the metadata backup to be older than its maximum age`);
            await cas.sleep(wait);
        }
        await clearMetadataCache();
        await assertSsoSucceeds(page, received, RESILIENT, "the metadata server is down, and CAS falls back onto a stale backup");
        entry = await cacheEntryFor(RESILIENT);
        assert.equal(entry.metadataBackupExists, true);
        assert.equal(entry.metadataBackupStale, true, "A backup older than metadata-backup-max-age must be reported stale");

        await assertSsoRefused(page, received, NEVER_LOADED, "the metadata server is down and there is no metadata backup");
        assert.equal(requestsFor(state, NEVER_LOADED).length, 0);
        assert(!fs.existsSync(metadataBackupFile(NEVER_LOADED)));
        entry = await cacheEntryFor(NEVER_LOADED);
        assert(`${entry.rememberedFailure}`.includes("no metadata backup file is available"),
            `The failure must be remembered with its reason, but got ${entry.rememberedFailure}`);
    } finally {
        if (server !== undefined) {
            stopMetadataServer(server);
        }
        await cas.closeBrowser(browser);
    }
}

(async () => {
    const endpoint = "https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadataCache";
    await cas.doDelete(endpoint);

    const GOOD_SP = 1;
    const BAD_SP = 2;
    const SERVICES = [GOOD_SP, BAD_SP];
    
    const sendMetadataRequest = async (id) => {
        try {
            const url = `${endpoint}?serviceId=${id}&includeMetadata=false`;
            return await cas.doGet(url, async (res) => {
                if (id === GOOD_SP) {
                    assert(res.status === 200);
                } else {
                    throw `Expected metadata resolution to pass for service id ${id}`;
                }
            }, async (err) => {
                if (id !== BAD_SP) {
                    await cas.logr(`Expected metadata resolution to pass for service id ${id}: ${err}`);
                    throw err;
                }
            }, {
                "Content-Type": "application/x-www-form-urlencoded",
                "Accept": "application/json"
            });
        } catch (err) {
            if (id === GOOD_SP) {
                await cas.logr(`Request for service id ${id} failed: ${err.message}`);
                throw err;
            }
        }
        return null;
    };

    const sendConcurrentRequests = async () => {
        const promises = [];
        for (let i = 1; i <= 150; i++) {
            const randomIndex = Math.floor(Math.random() * SERVICES.length);
            const serviceId = SERVICES[randomIndex];
            await cas.logb(`Sending metadata request for service id ${serviceId}`);
            promises.push(sendMetadataRequest(serviceId));
        }
        await Promise.all(promises);
    };
    await sendConcurrentRequests();

    await verifyMetadataBackupResilience();
})();

