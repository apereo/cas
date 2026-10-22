const assert = require("assert");
const cas = require("../../cas.js");

const ENDPOINT = "https://localhost:8443/cas/actuator/yubikeyAccountRepository";
const TABLE = "DynamoDbYubiKeyDevices";
const JSON_HEADERS = {"Content-Type": "application/json", "Accept": "application/json"};

async function importDevice(name, token) {
    const body = JSON.stringify({
        "@class": "org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest",
        "token": token,
        "name": name,
        "username": "casuser"
    });
    await cas.doRequest(`${ENDPOINT}/import`, "POST", JSON_HEADERS, 201, body);
}

/*
 * Reads the names of casuser's devices straight from the backend of the current variation. Redis values are compressed,
 * so for Redis only the presence of the account's key is checked and the names come back as undefined.
 */
async function storedDevices() {
    switch (process.env.SCENARIO_VARIATION) {
    case "jpa": {
        const sql = (query) => cas.dockerExec("mariadb", ["mariadb", "-uroot", "-pmypass", "-N", "-D", "mysql", "-e", query]);
        const table = (await sql("SELECT TABLE_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='mysql' "
            + "AND COLUMN_NAME LIKE 'public%id' AND TABLE_NAME LIKE '%yubi%registered%device%'")).split("\n")[0];
        assert(table.length > 0, "The YubiKey device table must exist");
        const names = await sql(`SELECT name FROM ${table}`);
        return names.length === 0 ? [] : names.split("\n");
    }
    case "mongo": {
        const output = await cas.dockerExec("mongodb-server", ["mongosh", "--quiet",
            "mongodb://root:secret@localhost:27017/yubikey?authSource=admin", "--eval",
            "EJSON.stringify(db.MongoDbYubiKeyRepository.find({username: 'casuser'}).toArray())"]);
        const accounts = JSON.parse(output.split("\n").pop());
        assert(accounts.length <= 1, "A user's devices are kept in a single document");
        return accounts.length === 0 ? [] : accounts[0].devices.map((device) => device.name);
    }
    case "redis": {
        const exists = await cas.dockerExec("redis_server_master", ["redis-cli", "EXISTS", "RedisYubiKeyAccountRegistry:casuser"]);
        return exists === "1" ? undefined : [];
    }
    case "dynamodb": {
        const items = (await cas.dynamoDbScan(TABLE)).filter((item) => item.username.S === "casuser");
        assert(items.length <= 1, "A user's devices are kept in a single item");
        return items.length === 0 ? [] : items[0].deviceIdentifiers.L.map((device) => device.M.name.S);
    }
    default:
        throw new Error(`Unknown variation ${process.env.SCENARIO_VARIATION}`);
    }
}

async function assertStoredDevices(expected) {
    const stored = await storedDevices();
    if (stored === undefined) {
        assert(expected.length > 0, "The account must be removed from the backend");
        return;
    }
    assert.deepStrictEqual([...stored].sort(), [...expected].sort());
}

(async () => {
    await cas.log(`Running with the ${process.env.SCENARIO_VARIATION} YubiKey repository`);
    await cas.doRequest(ENDPOINT, "DELETE", JSON_HEADERS, 200);
    await assertStoredDevices([]);

    await cas.logg("Devices registered through the actuator are kept in the backend");
    await importDevice("FirstDevice", "cccccccvlidcrcbuhflgeureketrlfkhdcfunflugdlt");
    await assertStoredDevices(["FirstDevice"]);
    await importDevice("SecondDevice", "ccccccbcgujhingjrdejhgfnuetrgigvejhhgbkugded");
    await assertStoredDevices(["FirstDevice", "SecondDevice"]);

    const account = JSON.parse(await cas.doRequest(`${ENDPOINT}/casuser`, "GET", JSON_HEADERS, 200));
    assert(account.username === "casuser");
    assert(account.devices.length === 2);
    assert(account.devices.every((device) => device.publicId.length > 0 && device.registrationDate !== undefined));
    const accounts = JSON.parse(await cas.doRequest(ENDPOINT, "GET", JSON_HEADERS, 200));
    assert(accounts.length === 1);

    await cas.logg("The login flow finds the registered devices and asks for a token");
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-yubikey");
        await cas.loginWith(page);
        await cas.sleep(1000);
        await cas.assertTextContent(page, "#login h3", "Use your registered YubiKey device(s) to authenticate.");
        await cas.assertVisibility(page, "#token");
    } finally {
        await cas.closeBrowser(browser);
    }

    await cas.logg("Removing the account removes it from the backend");
    await cas.doRequest(`${ENDPOINT}/casuser`, "DELETE", JSON_HEADERS, 200);
    await assertStoredDevices([]);
    await cas.doRequest(`${ENDPOINT}/casuser`, "GET", JSON_HEADERS, 200);
})();
