const cas = require("../../cas.js");
const assert = require("assert");

/*
 * Google Authenticator and trusted devices both keep their records in MongoDb, so the build carries two MongoDb
 * templates. Each is injected by name only, which keeps Spring Boot's own MongoDb support from finding two
 * candidates and stopping CAS from starting; the scenario shows that both stores work side by side.
 */
const GAUTH = "https://localhost:8443/cas/actuator/gauthCredentialRepository";
const TRUSTED = "https://localhost:8443/cas/actuator/multifactorTrustedDevices";

async function countDocuments(database, collection) {
    const output = await cas.dockerExec("mongodb-server", ["mongosh", "--quiet",
        `mongodb://root:secret@localhost:27017/${database}?authSource=admin`, "--eval",
        `db.${collection}.countDocuments({$or: [{username: 'casuser'}, {principal: 'casuser'}]})`]);
    return Number(output.split("\n").pop());
}

async function clear(database, collection) {
    await cas.dockerExec("mongodb-server", ["mongosh", "--quiet",
        `mongodb://root:secret@localhost:27017/${database}?authSource=admin`, "--eval", `db.${collection}.deleteMany({})`]);
}

(async () => {
    await cas.doRequest(GAUTH, "DELETE");
    await clear("trusted", "MongoDbCasTrustedAuthnMfaRepository");
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
        await cas.loginWith(page);
        await cas.sleep(2000);

        await cas.logg("A Google Authenticator device is registered and kept in MongoDb");
        const scratchCodes = await cas.innerTexts(page, "span[name='gauth-scratchcode']");
        await cas.click(page, "#confirm");
        await cas.assertVisibility(page, "#token");
        await cas.type(page, "#token", scratchCodes[0]);
        await cas.sleep(1000);
        await cas.click(page, "#registerButton");
        await cas.waitForElement(page, "#fm1");
        assert(await countDocuments("gauth", "MongoDbGoogleAuthenticatorRepository") === 1);

        await cas.logg("After the token, the browser is trusted and the device is kept in MongoDb");
        await cas.type(page, "#token", scratchCodes[1]);
        await cas.sleep(1000);
        await cas.pressEnter(page);
        await cas.waitForNavigation(page);
        await cas.sleep(1000);
        await cas.type(page, "#deviceName", "My Trusted Device");
        await cas.submitForm(page, "#registerform");
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
        await cas.gotoLogout(page);
        assert(await countDocuments("trusted", "MongoDbCasTrustedAuthnMfaRepository") === 1);
        const devices = JSON.parse(await cas.doRequest(TRUSTED, "GET", {"Accept": "application/json"}));
        assert(devices.some((device) => device.name === "My Trusted Device"));

        await cas.logg("The trusted browser skips Google Authenticator on the next login");
        await cas.gotoLoginWithAuthnMethod(page, undefined, "mfa-gauth");
        await cas.loginWith(page);
        await cas.sleep(3000);
        await cas.assertInvisibility(page, "#token");
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
        await cas.assertCookie(page);
    } finally {
        await cas.closeBrowser(browser);
    }
})();
