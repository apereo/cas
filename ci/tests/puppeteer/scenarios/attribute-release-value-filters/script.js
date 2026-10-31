
const assert = require("assert");
const cas = require("../../cas.js");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");

const PERSISTENT_ID_SALT = "v8bjJ5nBNiAdgrUh";

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await executeRequest(page, "https://localhost:9859/anything/1", "groupMembership1", "STUD");
    await executeRequest(page, "https://localhost:9859/anything/2", "groupMembership2", "ADMN");
    await executeRequest(page, "https://localhost:9859/anything/3", "groupMembership3", "FACULTY");
    await executeRequest(page, "https://localhost:9859/anything/4", "groupMembership4", "COURSE-H101");
    await executeRequest(page, "https://localhost:9859/anything/5", "COURSE", "CHEMISTRY-101");
    await executeRequest(page, "https://localhost:9859/anything/6", "COURSE", "SOFTENG-101");
    await verifyReleasePolicies(page);
    await verifyAnonymousUsername(page);
    await cas.closeBrowser(browser);
})();

async function executeRequest(page, service, attribute, attributeValue) {
    await cas.log(`Running tests for service ${service} with attribute requirements ${attribute}:${attributeValue}`);
    
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.logPage(page);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(service, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.user === "casuser");
    assert(authenticationSuccess.attributes.accountId === undefined);
    assert(authenticationSuccess.attributes.credentialType === undefined);
    assert(authenticationSuccess.attributes.authenticationDate === undefined);
    assert(authenticationSuccess.attributes.isFromNewLogin === undefined);
    assert(authenticationSuccess.attributes.longTermAuthenticationRequestTokenUsed === undefined);
    assert(authenticationSuccess.attributes[attribute][0] === attributeValue);
    await cas.gotoLogout(page);
    await cas.sleep(1000);
    await cas.separator();
    
    return authenticationSuccess;
}

/**
 * Logs in to a service and validates its service ticket.
 *
 * @param page the page
 * @param service the service URL
 * @returns the successful validation response
 */
async function validateService(page, service) {
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(1000);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(service, ticket);
    await cas.gotoLogout(page);
    return json.serviceResponse.authenticationSuccess;
}

/**
 * Decrypts a value encrypted with the public key of the service. RSA is used without padding and the PKCS#1 v1.5
 * padding is removed here, because Node no longer decrypts that padding.
 *
 * @param value the base64 encoded value
 * @returns the plain value
 */
function decrypt(value) {
    const block = crypto.privateDecrypt(
        {
            key: fs.readFileSync(path.join(__dirname, "private.key"), "utf8"),
            padding: crypto.constants.RSA_NO_PADDING
        },
        Buffer.from(value, "base64"));
    return block.subarray(block.indexOf(0, 2) + 1).toString("utf8");
}

/**
 * Release policies that encrypt values, transform them by pattern, source them from linked attributes, read them
 * from the environment and build them in a Groovy script.
 *
 * @param page the page
 */
async function verifyReleasePolicies(page) {
    await cas.logg("Encrypted release: values are encrypted with the public key of the service");
    let success = await validateService(page, "https://localhost:9859/anything/encrypted");
    assert.notEqual(success.attributes.accountId[0], "9Wy1B3m3f66bPPmU5");
    assert.equal(decrypt(success.attributes.accountId[0]), "9Wy1B3m3f66bPPmU5");

    await cas.logg("Pattern matching release: matching values are transformed and the others are not released");
    success = await validateService(page, "https://localhost:9859/anything/pattern");
    assert.deepEqual([...success.attributes.courses].sort(), ["CHEM-202", "MATH-101"]);
    assert.equal(success.attributes.accountId, undefined);

    await cas.logg("Linked release: the first linked attribute with a value is the source");
    success = await validateService(page, "https://localhost:9859/anything/linked");
    assert.deepEqual(success.attributes.primaryGroup, ["ADMN"]);
    assert.equal(success.attributes.groupMembership1, undefined);

    await cas.logg("Environment release: environment variables are released under the names given to them");
    success = await validateService(page, "https://localhost:9859/anything/environment");
    assert.deepEqual(success.attributes.scenarioName, ["attribute-release-value-filters"]);

    await cas.logg("Groovy script release: the script builds the released attributes");
    success = await validateService(page, "https://localhost:9859/anything/groovy");
    assert.deepEqual(success.attributes.displayName, ["CAS casuser"]);
    assert.deepEqual(success.attributes.faculty, ["FACULTY"]);
    assert.equal(success.attributes.accountId, undefined);
}

/**
 * The anonymous username provider releases a persistent identifier instead of the username: the same for a user
 * at one service every time, different at another service.
 *
 * @param page the page
 */
async function verifyAnonymousUsername(page) {
    await cas.logg("Anonymous username: a persistent identifier per user and service");
    const expected = (service) => crypto.createHash("sha1")
        .update(service).update("!").update("casuser").update("!").update(PERSISTENT_ID_SALT)
        .digest("base64");
    const first = "https://localhost:9859/anything/anonymous-a";
    const second = "https://localhost:9859/anything/anonymous-b";
    const firstUser = (await validateService(page, first)).user;
    assert.equal(firstUser, expected(first));
    assert.equal((await validateService(page, first)).user, firstUser);
    const secondUser = (await validateService(page, second)).user;
    assert.equal(secondUser, expected(second));
    assert.notEqual(firstUser, secondUser);
}
