const assert = require("assert");
const cas = require("../../cas.js");

const DEVICES = "https://localhost:8443/cas/actuator/webAuthnDevices/casuser";

/**
 * Reads what the repository of the current variation keeps for casuser, straight from the backend. Registration
 * records are stored unencrypted in this scenario, so the device nickname shows in them.
 */
async function storedRecords() {
    switch (process.env.SCENARIO_VARIATION) {
    case "jpa": {
        const table = await cas.dockerExec("mariadb", ["mariadb", "-uroot", "-pmypass", "-N", "-e",
            "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='mysql' AND TABLE_NAME LIKE '%WebAuthn%'"]);
        assert(table.length > 0, "WebAuthn table must exist");
        return cas.dockerExec("mariadb", ["mariadb", "-uroot", "-pmypass", "-N", "-D", "mysql", "-e",
            `SELECT records FROM ${table.split("\n")[0]} WHERE username='casuser'`]);
    }
    case "mongo":
        return cas.dockerExec("mongodb-server", ["mongosh", "--quiet",
            "mongodb://root:secret@localhost:27017/webauthn?authSource=admin", "--eval",
            "EJSON.stringify(db.MongoDbWebAuthnRepository.find({username: 'casuser'}).toArray())"]);
    case "redis":
        return cas.dockerExec("redis_server_master", ["redis-cli", "GET", "RedisWebAuthnCredentialRepository:casuser"]);
    default:
        throw new Error(`Unknown variation ${process.env.SCENARIO_VARIATION}`);
    }
}

(async () => {
    await cas.log(`Running with the ${process.env.SCENARIO_VARIATION} repository`);
    await cas.doRequest(DEVICES, "DELETE", {"Accept": "application/json"}, 200);
    assert(!(await storedRecords()).includes("mydevice"));

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

    const devices = JSON.parse(await cas.doRequest(DEVICES, "GET", {"Accept": "application/json"}, 200));
    assert(devices.length === 1);
    assert(devices[0].credentialNickname === "mydevice");
    assert((await storedRecords()).includes("mydevice"), "The registration must be kept in the backend");

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
    assert(JSON.parse(await cas.doRequest(DEVICES, "GET", {"Accept": "application/json"}, 200)).length === 0);
    assert(!(await storedRecords()).includes("mydevice"), "Removing the device must remove it from the backend");

    await cas.sleep(2000);
    await cas.removeWebAuthnVirtualAuthenticator(virtualAuthenticator);
    await cas.closeBrowser(browser);
})();
