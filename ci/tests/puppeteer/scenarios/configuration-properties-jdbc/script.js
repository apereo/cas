
const cas = require("../../cas.js");
const assert = require("assert");

const ACTUATOR = "https://localhost:8443/cas/actuator";
const SERVICE = "https://localhost:9859/anything/cas";
const PROPERTY = "cas.logout.follow-service-redirects";
const SOURCE = "bootstrapProperties-JdbcPropertySource";

async function psql(sql) {
    return cas.dockerExec("postgres-server", ["psql", "-U", "postgres", "-d", "casconfig", "-t", "-A", "-c", sql]);
}

/**
 * An encrypted value kept in the settings table is decrypted into the casConfigurationDecryptedProperties source
 * once CAS refreshes (CasConfigurationDecryptionInitializer), while the table keeps the encrypted value. A retry
 * reuses the database and CAS, so a value left by an earlier attempt is reset to false first: a removed property
 * may not be unbound from the settings CAS already holds.
 */
async function verifyEncryptedProperty(page) {
    if (await psql(`SELECT COUNT(*) FROM CAS_SETTINGS_TABLE WHERE name='${PROPERTY}'`) !== "0") {
        await psql(`UPDATE CAS_SETTINGS_TABLE SET value='false' WHERE name='${PROPERTY}'`);
        await cas.refreshContext();
        await cas.sleep(2000);
    }
    await cas.gotoLogout(page, SERVICE);
    await cas.sleep(1000);
    await cas.assertPageUrlStartsWith(page, "https://localhost:8443/cas/logout");

    await cas.logg("An encrypted value stored in the database is applied decrypted after a refresh");
    const encrypted = await cas.doRequest(`${ACTUATOR}/casConfig/encrypt`, "POST", {"Content-Type": "text/plain"}, 200, "true");
    await cas.log(`Encrypted value: ${encrypted}`);
    assert(encrypted.startsWith("{cas-cipher}"));
    const updated = JSON.parse(await cas.doRequest(`${ACTUATOR}/casConfig/update`, "POST",
        {"Content-Type": "application/json", "Accept": "application/json"}, 200,
        JSON.stringify([{name: PROPERTY, value: encrypted}])));
    await cas.log(updated);
    assert(updated.some((source) => source.endsWith("JdbcPropertySource")));
    assert.equal(await psql(`SELECT value FROM CAS_SETTINGS_TABLE WHERE name='${PROPERTY}'`), encrypted);

    await cas.refreshContext();
    await cas.sleep(2000);
    const json = JSON.parse(await cas.doRequest(`${ACTUATOR}/env/${PROPERTY}`, "GET", {"Accept": "application/json"}, 200));
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
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, SERVICE);
    await cas.loginWith(page, "postgrescas", "p@SSw0rd");
    await cas.assertTicketParameter(page);
    await cas.gotoLogin(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.gotoLogout(page);
    await verifyEncryptedProperty(page);
    await cas.closeBrowser(browser);
})();
