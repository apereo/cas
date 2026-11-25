const cas = require("../../cas.js");
const assert = require("assert");

const ACTUATOR = "https://localhost:8443/cas/actuator";
const SOURCE = "bootstrapProperties-EtcdPropertySource";
const SERVICE = "https://localhost:9859/anything/cas";
const PREFIX = "/cas/config/default";
const USERS = "cas.authn.accept.users";
const ENCRYPTED_PROPERTY = "cas.logout.follow-service-redirects";

async function etcdGet(name) {
    return cas.dockerExec("etcd-server", ["etcdctl", "get", "--print-value-only", `${PREFIX}/${name}`]);
}

async function etcdPut(name, value) {
    await cas.dockerExec("etcd-server", ["etcdctl", "put", `${PREFIX}/${name}`, value]);
}

async function assertProperty(name, value, source = SOURCE) {
    const json = JSON.parse(await cas.doRequest(`${ACTUATOR}/env/${name}`, "GET", {"Accept": "application/json"}, 200));
    await cas.log(json.property);
    assert.equal(json.property.source, source);
    assert.equal(json.property.value, value);
    return json;
}

async function login(page, username, success) {
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page, username, "Mellon");
    await cas.sleep(1000);
    await cas.assertCookie(page, success);
}

/**
 * A retry reuses etcd and CAS, so values changed by an earlier attempt are put back first. A removed property may not
 * be unbound from the settings CAS already holds, so the encrypted setting is reset to false rather than removed.
 */
async function resetSettings() {
    if (await etcdGet(USERS) !== "etcduser::Mellon" || await etcdGet(ENCRYPTED_PROPERTY) !== "false") {
        await etcdPut(USERS, "etcduser::Mellon");
        await etcdPut(ENCRYPTED_PROPERTY, "false");
        await cas.refreshContext();
        await cas.sleep(2000);
    }
}

/**
 * An encrypted value written through CAS lands in etcd as it is, and is decrypted into the
 * casConfigurationDecryptedProperties source once CAS refreshes (CasConfigurationDecryptionInitializer), while the
 * etcd source keeps the encrypted value.
 */
async function verifyEncryptedProperty(page) {
    await assertProperty(ENCRYPTED_PROPERTY, "false");
    await cas.gotoLogout(page, SERVICE);
    await cas.sleep(1000);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/logout");

    const encrypted = await cas.doRequest(`${ACTUATOR}/casConfig/encrypt`, "POST", {"Content-Type": "text/plain"}, 200, "true");
    await cas.log(`Encrypted value: ${encrypted}`);
    assert(encrypted.startsWith("{cas-cipher}"));
    const updated = JSON.parse(await cas.doRequest(`${ACTUATOR}/casConfig/update`, "POST",
        {"Content-Type": "application/json", "Accept": "application/json"}, 200,
        JSON.stringify([{name: ENCRYPTED_PROPERTY, value: encrypted, propertySource: "EtcdPropertySource"}])));
    await cas.log(updated);
    assert.deepEqual(updated, ["EtcdPropertySource"]);
    assert.equal(await etcdGet(ENCRYPTED_PROPERTY), encrypted);

    await cas.refreshContext();
    await cas.sleep(2000);
    const json = await assertProperty(ENCRYPTED_PROPERTY, "true", "casConfigurationDecryptedProperties");
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
        await resetSettings();

        await cas.logg("Properties seeded in etcd before startup are applied");
        await assertProperty(USERS, "etcduser::Mellon");
        await login(page, "etcduser", true);

        await cas.logg("A value changed in etcd is picked up after a refresh, and only then");
        await etcdPut(USERS, "etcduser2::Mellon");
        await login(page, "etcduser2", false);
        await cas.refreshContext();
        await cas.sleep(2000);
        await assertProperty(USERS, "etcduser2::Mellon");
        await login(page, "etcduser", false);
        await login(page, "etcduser2", true);

        await cas.logg("An encrypted value stored in etcd is applied decrypted after a refresh");
        await verifyEncryptedProperty(page);
    } finally {
        await cas.closeBrowser(browser);
    }
})();
