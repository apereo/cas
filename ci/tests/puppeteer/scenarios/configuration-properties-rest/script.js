const cas = require("../../cas.js");
const assert = require("assert");

const ACTUATOR = "https://localhost:8443/cas/actuator";
const BACKEND = "http://localhost:5618/config";
const SOURCE = "bootstrapProperties-org.apereo.cas.config.RestfulPropertySource";
const SERVICE = "https://localhost:9859/anything/cas";
const ENCRYPTED_PROPERTY = "cas.logout.follow-service-redirects";

async function assertProperty(name, value) {
    const json = JSON.parse(await cas.doRequest(`${ACTUATOR}/env/${name}`, "GET", {"Accept": "application/json"}, 200));
    await cas.log(json.property);
    assert(json.property.source === SOURCE);
    assert(json.property.value === value);
}

async function login(page, username, success) {
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page, username, "Mellon");
    await cas.sleep(1000);
    await cas.assertCookie(page, success);
}

/**
 * An encrypted value kept in the backend is decrypted into the casConfigurationDecryptedProperties source once CAS
 * refreshes (CasConfigurationDecryptionInitializer), while the REST source still answers with the encrypted value.
 * A retry reuses the backend and CAS, so a value left by an earlier attempt is reset to false first: a removed
 * property may not be unbound from the settings CAS already holds.
 */
async function verifyEncryptedProperty(page) {
    const names = JSON.parse(await cas.doRequest(`${BACKEND}/names`, "GET", {"Accept": "application/json"}, 200));
    if (names.includes(ENCRYPTED_PROPERTY)) {
        await cas.doRequest(`${BACKEND}?name=${ENCRYPTED_PROPERTY}&value=false`, "POST", {}, 200);
        await cas.refreshContext();
        await cas.sleep(2000);
    }
    await cas.gotoLogout(page, SERVICE);
    await cas.sleep(1000);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/logout");

    const encrypted = await cas.doRequest(`${ACTUATOR}/casConfig/encrypt`, "POST", {"Content-Type": "text/plain"}, 200, "true");
    await cas.log(`Encrypted value: ${encrypted}`);
    assert(encrypted.startsWith("{cas-cipher}"));
    await cas.doRequest(`${BACKEND}?name=${ENCRYPTED_PROPERTY}&value=${encodeURIComponent(encrypted)}`, "POST", {}, 200);
    assert.equal(await cas.doRequest(`${BACKEND}?name=${ENCRYPTED_PROPERTY}`, "GET", {}, 200), encrypted);

    await cas.refreshContext();
    await cas.sleep(2000);
    const json = JSON.parse(await cas.doRequest(`${ACTUATOR}/env/${ENCRYPTED_PROPERTY}`, "GET", {"Accept": "application/json"}, 200));
    await cas.log(json.property);
    assert.equal(json.property.source, "casConfigurationDecryptedProperties");
    assert.equal(json.property.value, "true");
    const stored = json.propertySources.find((source) => source.name === SOURCE);
    assert.equal(stored?.property?.value, encrypted);

    await cas.gotoLogout(page, SERVICE);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, SERVICE);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        await cas.logg("Properties are read from the REST backend at startup");
        await assertProperty("cas.authn.accept.users", "restuser::Mellon");
        await assertProperty("cas.server.scope", "example.org");
        await login(page, "restuser", true);

        await cas.logg("A value changed in the backend is picked up after a refresh");
        await cas.doRequest(`${BACKEND}?name=cas.authn.accept.users&value=${encodeURIComponent("restuser2::Mellon")}`, "POST", {}, 200);
        await cas.refreshContext();
        await cas.sleep(2000);
        await assertProperty("cas.authn.accept.users", "restuser2::Mellon");
        await login(page, "restuser", false);
        await login(page, "restuser2", true);

        await cas.logg("A value updated through CAS is written to the backend");
        const updated = JSON.parse(await cas.doRequest(`${ACTUATOR}/casConfig/update`, "POST",
            {"Content-Type": "application/json", "Accept": "application/json"}, 200,
            JSON.stringify([{name: "cas.server.scope", value: "example.com"}])));
        await cas.log(updated);
        assert(updated.some((source) => source.endsWith("RestfulPropertySource")));
        assert(await cas.doRequest(`${BACKEND}?name=cas.server.scope`, "GET", {}, 200) === "example.com");
        await cas.refreshContext();
        await cas.sleep(2000);
        await assertProperty("cas.server.scope", "example.com");

        await cas.logg("An encrypted value stored in the backend is applied decrypted after a refresh");
        await verifyEncryptedProperty(page);
    } finally {
        await cas.closeBrowser(browser);
    }
})();
