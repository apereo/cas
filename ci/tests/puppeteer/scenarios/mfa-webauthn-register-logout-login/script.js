const assert = require("assert");
const cas = require("../../cas.js");
const express = require("express");

const DEVICES = "https://localhost:8443/cas/actuator/webAuthnDevices/casuser";

/*
 * The rest variation keeps, per user, the whole collection of registrations CAS last posted with ?username=.
 * A GET with ?username= returns that collection and a GET without it returns every registration.
 */
const restRegistrations = new Map();

/**
 * Starts the REST WebAuthn backend the rest variation points CAS at.
 *
 * @returns the running server, or undefined for other variations
 */
async function startRestBackend() {
    if (process.env.SCENARIO_VARIATION !== "rest") {
        return undefined;
    }
    const app = express();
    app.use(express.text({type: "*/*"}));
    app.get("/webauthn", (req, res) => {
        const username = req.query.username;
        const records = username === undefined
            ? [...restRegistrations.values()].flatMap((raw) => JSON.parse(raw))
            : JSON.parse(restRegistrations.get(username) ?? "[]");
        res.status(200).type("application/json").send(JSON.stringify(records));
    });
    app.post("/webauthn", (req, res) => {
        cas.log(`Storing WebAuthn registrations for ${req.query.username}: ${req.body}`);
        restRegistrations.set(req.query.username, req.body);
        res.sendStatus(200);
    });
    return new Promise((resolve) => {
        const server = app.listen(5613, () => resolve(server));
    });
}

/**
 * Whether the repository of the current variation keeps a registration for casuser, read straight from the backend.
 * Registration records are stored unencrypted in this scenario, so a stored registration shows its credentialId;
 * Redis values are compressed, so there only the key is checked, which the repository deletes with the last device.
 */
async function hasStoredRegistration() {
    switch (process.env.SCENARIO_VARIATION) {
    case "jpa": {
        const table = await cas.dockerExec("mariadb", ["mariadb", "-uroot", "-pmypass", "-N", "-e",
            "SELECT TABLE_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='mysql' AND COLUMN_NAME='records' AND TABLE_NAME LIKE '%web%authn%'"]);
        assert(table.length > 0, "WebAuthn table must exist");
        const records = await cas.dockerExec("mariadb", ["mariadb", "-uroot", "-pmypass", "-N", "-D", "mysql", "-e",
            `SELECT records FROM ${table.split("\n")[0]} WHERE username='casuser'`]);
        return records.includes("credentialId");
    }
    case "mongo":
        return (await cas.dockerExec("mongodb-server", ["mongosh", "--quiet",
            "mongodb://root:secret@localhost:27017/webauthn?authSource=admin", "--eval",
            "EJSON.stringify(db.MongoDbWebAuthnRepository.find({username: 'casuser'}).toArray())"])).includes("credentialId");
    case "redis":
        return (await cas.dockerExec("redis_server_master", ["redis-cli", "EXISTS", "RedisWebAuthnCredentialRepository:casuser"])) === "1";
    case "dynamodb": {
        const items = await cas.dynamoDbScan("DynamoDbCasWebAuthnRecords");
        return JSON.stringify(items.filter((item) => item.principal.S === "casuser")).includes("credentialId");
    }
    case "rest":
        return (restRegistrations.get("casuser") ?? "").includes("credentialId");
    default:
        throw new Error(`Unknown variation ${process.env.SCENARIO_VARIATION}`);
    }
}

(async () => {
    await cas.log(`Running with the ${process.env.SCENARIO_VARIATION} repository`);
    const restBackend = await startRestBackend();
    await cas.doRequest(DEVICES, "DELETE", {"Accept": "application/json"}, 200);
    assert(!(await hasStoredRegistration()));

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const virtualAuthenticator = await cas.createWebAuthnVirtualAuthenticator(page);

    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-webauthn");

    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertTextContent(page, "#status", "Register Device");
    await cas.assertVisibility(page, "#messages");
    await cas.assertInvisibility(page, "#device-info");
    await cas.assertInvisibility(page, "#device-icon");
    await cas.assertInvisibility(page, "#device-name");
    await cas.assertVisibility(page, "#credentialNickname");
    await cas.assertVisibility(page, "#registerButton");
    await cas.assertVisibility(page, "#residentKeysPanel");
    await cas.assertVisibility(page, "#registerDiscoverableCredentialButton");

    await page.click("#credentialNickname", {clickCount: 3});
    await cas.pressBackspace(page);
    await page.type("#credentialNickname", "mydevice");

    await cas.click(page,"#registerButton");

    await cas.sleep(5000);

    await cas.click(page, "#authnButton");

    await cas.sleep(5000);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.click(page, "#auth-tab");
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnAuthenticationHandler]");
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[mfa-webauthn]");
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnCredential]");

    const devices = await cas.doRequest(DEVICES, "GET", {"Accept": "application/json"}, 200);
    await cas.log(devices);
    assert(devices.includes("credentialId"), "The registered device must be listed");
    assert(await hasStoredRegistration(), "The registration must be kept in the backend");

    await cas.gotoLogout(page);

    await cas.sleep(1000);
    await cas.assertPageUrl(page, "https://localhost:8443/cas/logout");
    await cas.assertCookie(page, false);
    await cas.assertInnerText(page, "#main-content h2", "Logout successful");

    await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-webauthn");

    await cas.loginWith(page);
    await cas.sleep(1000);

    await cas.click(page, "#authnButton");

    await cas.sleep(5000);

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.click(page, "#auth-tab");
    await cas.sleep(1000);
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnAuthenticationHandler]");
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[mfa-webauthn]");
    await cas.assertInnerTextContains(page, "#attribute-tab-1 table#attributesTable tbody", "[WebAuthnCredential]");

    await cas.doRequest(DEVICES, "DELETE", {"Accept": "application/json"}, 200);
    assert(!(await cas.doRequest(DEVICES, "GET", {"Accept": "application/json"}, 200)).includes("credentialId"));
    assert(!(await hasStoredRegistration()), "Removing the device must remove it from the backend");

    await cas.sleep(2000);
    await cas.removeWebAuthnVirtualAuthenticator(virtualAuthenticator);
    await cas.closeBrowser(browser);
    restBackend?.close();
})();
