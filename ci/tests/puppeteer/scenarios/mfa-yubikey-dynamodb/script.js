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

async function storedDevices() {
    const items = (await cas.dynamoDbScan(TABLE)).filter((item) => item.username.S === "casuser");
    assert(items.length <= 1, "A user's devices are kept in a single item");
    return items.length === 0 ? [] : items[0].deviceIdentifiers.L.map((device) => device.M.name.S);
}

(async () => {
    await cas.doRequest(ENDPOINT, "DELETE", JSON_HEADERS, 200);
    assert((await storedDevices()).length === 0);

    await cas.logg("Devices registered through the actuator are kept in DynamoDb");
    await importDevice("FirstDevice", "cccccccvlidcrcbuhflgeureketrlfkhdcfunflugdlt");
    assert.deepStrictEqual(await storedDevices(), ["FirstDevice"]);
    await importDevice("SecondDevice", "ccccccbcgujhingjrdejhgfnuetrgigvejhhgbkugded");
    assert.deepStrictEqual((await storedDevices()).sort(), ["FirstDevice", "SecondDevice"]);

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

    await cas.logg("Removing the account removes it from DynamoDb");
    await cas.doRequest(`${ENDPOINT}/casuser`, "DELETE", JSON_HEADERS, 200);
    assert((await storedDevices()).length === 0);
    await cas.doRequest(`${ENDPOINT}/casuser`, "GET", JSON_HEADERS, 200);
})();
