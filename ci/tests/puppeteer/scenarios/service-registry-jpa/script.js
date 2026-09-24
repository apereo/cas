const assert = require("assert");
const cas = require("../../cas.js");
const fs = require("fs");
const path = require("path");

const TOTAL = 10;
const ACTUATOR_URL = "https://localhost:8443/cas/actuator";
const BASE_URL = `${ACTUATOR_URL}/registeredServices`;

async function fetchServices() {
    await cas.log("Fetching services from CAS");
    const body = await cas.doRequest(BASE_URL, "GET",
        {
            "Content-Type": "application/json",
            "Accept": "application/json"
        }, 200);
    await cas.log(body);
    await cas.separator();
    return body;
}

async function verifyServices() {
    await cas.log("Verifying services from CAS");
    for (let i = 1; i <= TOTAL; i++) {
        await cas.doGet(`${BASE_URL}/${i}`,
            async () => {
            }, async (error) => {
                throw error;
            }, {"Content-Type": "application/json"});
    }
}

async function importServices() {
    const template = path.join(__dirname, "registered-service.json");
    const contents = fs.readFileSync(template, "utf8");
    for (let i = 1; i <= TOTAL; i++) {
        const serviceId = String(i);
        const serviceBody = contents.replaceAll("${id}", serviceId);
        await cas.log(`Import registered service:\n${serviceBody}`);
        await cas.doRequest(`${BASE_URL}/import`, "POST", {
            "Accept": "application/json",
            "Content-Length": serviceBody.length,
            "Content-Type": "application/json"
        }, 201, serviceBody);

        await cas.doRequest(`${ACTUATOR_URL}/entityHistory/registeredServices/${serviceId}`, "GET", {
            "Accept": "application/json",
            "Content-Type": "application/json"
        });
        await cas.doRequest(`${ACTUATOR_URL}/entityHistory/registeredServices/${serviceId}/changelog`, "GET", {
            "Accept": "text/plain",
            "Content-Type": "application/json"
        });
    }
}

async function verifyHistoryRestore() {
    const headers = {"Accept": "application/json", "Content-Type": "application/json"};
    const historyUrl = `${ACTUATOR_URL}/entityHistory/registeredServices/1`;
    const history = JSON.parse(await cas.doRequest(historyUrl, "GET", headers));
    assert(history.length > 0);
    const original = history[0];
    const service = JSON.parse(await cas.doRequest(`${BASE_URL}/1`, "GET", headers));
    service.description = "Updated service before restoring history";
    service.attributeReleasePolicy = {"@class": "org.apereo.cas.services.DenyAllAttributeReleasePolicy"};
    await cas.doRequest(`${BASE_URL}/import`, "POST", headers, 201, JSON.stringify(service));

    const updated = JSON.parse(await cas.doRequest(`${BASE_URL}/1`, "GET", headers));
    assert.strictEqual(updated.description, service.description);
    assert.strictEqual(updated.attributeReleasePolicy["@class"], service.attributeReleasePolicy["@class"]);

    const otherHistory = JSON.parse(await cas.doRequest(`${ACTUATOR_URL}/entityHistory/registeredServices/2`, "GET", headers));
    await cas.doRequest(`${historyUrl}/restore/${encodeURIComponent(otherHistory[0].id)}`, "POST", headers, 404);
    await cas.doRequest(`${historyUrl}/restore/unknown`, "POST", headers, 404);
    assert.deepStrictEqual(JSON.parse(await cas.doRequest(`${BASE_URL}/1`, "GET", headers)), updated);

    const restored = JSON.parse(await cas.doRequest(`${historyUrl}/restore/${encodeURIComponent(original.id)}`, "POST", headers));
    assert.strictEqual(restored.id, 1);
    assert.strictEqual(restored.description, original.entity.description);
    assert.deepStrictEqual(restored.attributeReleasePolicy, original.entity.attributeReleasePolicy);

    await fetchServices();
    const reloaded = JSON.parse(await cas.doRequest(`${BASE_URL}/1`, "GET", headers));
    assert.deepStrictEqual(reloaded, restored);

    const restoredHistory = JSON.parse(await cas.doRequest(historyUrl, "GET", headers));
    assert.strictEqual(restoredHistory.length, history.length + 2);
    assert.notStrictEqual(restoredHistory[0].id, original.id);
    assert.strictEqual(restoredHistory[0].entity.description, original.entity.description);
    assert.strictEqual(restoredHistory[1].entity.description, updated.description);
    assert(restoredHistory.some((revision) => revision.id === original.id));
}

(async () => {
    let failed = false;
    try {
        const mysql = await cas.dockerContainer("mysql-server");

        await importServices();
        await verifyHistoryRestore();
        await fetchServices();

        await cas.log("Pausing MySQL docker container");
        await mysql.pause();

        await verifyServices();

        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);
        const service = "https://localhost:9859/anything/cas";
        await cas.gotoLogin(page, service);
        await cas.loginWith(page);
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(service, ticket);
        const authenticationSuccess = json.serviceResponse.authenticationSuccess;
        assert(authenticationSuccess.user === "casuser");
        await cas.gotoLogout(page);
        await cas.closeBrowser(browser);

        await cas.log("Unpausing MySQL docker container");
        await mysql.unpause();

        await cas.sleep(2000);
        await fetchServices();
        await verifyServices();
        await cas.logg("All CAS services are available");

    } catch (e) {
        failed = true;
        throw e;
    } finally {
        if (!failed) {
            await process.exit(0);
        }
    }
})();
