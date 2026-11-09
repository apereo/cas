
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");
const assert = require("assert");
const cas = require("../../cas.js");

const XMLENC = "http://www.w3.org/2001/04/xmlenc#";
const XMLENC11 = "http://www.w3.org/2009/xmlenc11#";
const ENTITY_PREFIX = "https://encryption.example.org";

const HASHES = {
    "http://www.w3.org/2000/09/xmldsig#sha1": "sha1",
    "http://www.w3.org/2001/04/xmldsig-more#sha224": "sha224",
    "http://www.w3.org/2001/04/xmlenc#sha256": "sha256",
    "http://www.w3.org/2001/04/xmldsig-more#sha384": "sha384",
    "http://www.w3.org/2001/04/xmlenc#sha512": "sha512",
    "http://www.w3.org/2009/xmlenc11#mgf1sha1": "sha1",
    "http://www.w3.org/2009/xmlenc11#mgf1sha224": "sha224",
    "http://www.w3.org/2009/xmlenc11#mgf1sha256": "sha256",
    "http://www.w3.org/2009/xmlenc11#mgf1sha384": "sha384",
    "http://www.w3.org/2009/xmlenc11#mgf1sha512": "sha512"
};

const DATA_ALGORITHMS = {
    [`${XMLENC}aes128-cbc`]: {cipher: "aes-128-cbc", ivLength: 16, gcm: false},
    [`${XMLENC}aes192-cbc`]: {cipher: "aes-192-cbc", ivLength: 16, gcm: false},
    [`${XMLENC}aes256-cbc`]: {cipher: "aes-256-cbc", ivLength: 16, gcm: false},
    [`${XMLENC11}aes128-gcm`]: {cipher: "aes-128-gcm", ivLength: 12, gcm: true},
    [`${XMLENC11}aes192-gcm`]: {cipher: "aes-192-gcm", ivLength: 12, gcm: true},
    [`${XMLENC11}aes256-gcm`]: {cipher: "aes-256-gcm", ivLength: 12, gcm: true}
};

function children(node, localName) {
    if (node === undefined || node === null || typeof node !== "object") {
        return [];
    }
    return Object.keys(node)
        .filter((key) => key !== "$" && key !== "_" && (key === localName || key.endsWith(`:${localName}`)))
        .flatMap((key) => node[key]);
}

function child(node, localName) {
    return children(node, localName)[0];
}

function textOf(node) {
    return typeof node === "string" ? node : node["_"];
}

function attributeOf(node, name) {
    return node?.["$"]?.[name];
}

function digestOf(algorithm, data) {
    return crypto.createHash(algorithm).update(data).digest();
}

function mgf1(seed, length, algorithm) {
    const blocks = [];
    let total = 0;
    for (let counter = 0; total < length; counter++) {
        const suffix = Buffer.alloc(4);
        suffix.writeUInt32BE(counter);
        const block = digestOf(algorithm, Buffer.concat([seed, suffix]));
        blocks.push(block);
        total += block.length;
    }
    return Buffer.concat(blocks).subarray(0, length);
}

function xor(left, right) {
    return Buffer.from(left.map((value, index) => value ^ right[index]));
}

/*
 * Node applies the OAEP digest to MGF1 as well, while XML encryption may use different ones
 * (rsa-oaep-mgf1p always uses MGF1 with SHA-1), so the padding is removed here after a raw RSA decryption.
 */
function removeOaepPadding(encoded, digest, mgfDigest, label) {
    const hashLength = digestOf(digest, Buffer.alloc(0)).length;
    assert.equal(encoded[0], 0, "OAEP encoded message must start with a zero byte");
    const maskedSeed = encoded.subarray(1, 1 + hashLength);
    const maskedDb = encoded.subarray(1 + hashLength);
    const seed = xor(maskedSeed, mgf1(maskedDb, hashLength, mgfDigest));
    const db = xor(maskedDb, mgf1(seed, maskedDb.length, mgfDigest));
    assert(db.subarray(0, hashLength).equals(digestOf(digest, label)), "OAEP label hash does not match");
    let index = hashLength;
    while (index < db.length && db[index] === 0) {
        index++;
    }
    assert.equal(db[index], 1, "OAEP separator byte is missing");
    return db.subarray(index + 1);
}

function decryptKey(encryptedKey, privateKey) {
    const method = child(encryptedKey, "EncryptionMethod");
    const algorithm = attributeOf(method, "Algorithm");
    const cipherValue = Buffer.from(textOf(child(child(encryptedKey, "CipherData"), "CipherValue")), "base64");
    const decrypted = crypto.privateDecrypt({key: privateKey, padding: crypto.constants.RSA_NO_PADDING}, cipherValue);
    if (algorithm === `${XMLENC}rsa-1_5`) {
        return decrypted.subarray(decrypted.indexOf(0, 2) + 1);
    }
    const digest = HASHES[attributeOf(child(method, "DigestMethod"), "Algorithm")] ?? "sha1";
    const mgfDigest = algorithm === `${XMLENC}rsa-oaep-mgf1p`
        ? "sha1"
        : HASHES[attributeOf(child(method, "MGF"), "Algorithm")] ?? "sha1";
    const oaepParams = child(method, "OAEPparams");
    const label = oaepParams === undefined ? Buffer.alloc(0) : Buffer.from(textOf(oaepParams), "base64");
    return removeOaepPadding(decrypted, digest, mgfDigest, label);
}

function decryptData(encryptedData, key) {
    const algorithm = attributeOf(child(encryptedData, "EncryptionMethod"), "Algorithm");
    const spec = DATA_ALGORITHMS[algorithm];
    assert(spec !== undefined, `Unsupported data encryption algorithm ${algorithm}`);
    const value = Buffer.from(textOf(child(child(encryptedData, "CipherData"), "CipherValue")), "base64");
    const iv = value.subarray(0, spec.ivLength);
    const decipher = crypto.createDecipheriv(spec.cipher, key, iv);
    if (spec.gcm) {
        decipher.setAuthTag(value.subarray(value.length - 16));
        return Buffer.concat([decipher.update(value.subarray(spec.ivLength, value.length - 16)), decipher.final()]).toString("utf8");
    }
    decipher.setAutoPadding(false);
    const padded = Buffer.concat([decipher.update(value.subarray(spec.ivLength)), decipher.final()]);
    return padded.subarray(0, padded.length - padded[padded.length - 1]).toString("utf8");
}

function decryptElement(element, privateKey) {
    const encryptedData = child(element, "EncryptedData");
    assert(encryptedData !== undefined, "No EncryptedData is found");
    const encryptedKey = child(element, "EncryptedKey") ?? child(child(encryptedData, "KeyInfo"), "EncryptedKey");
    assert(encryptedKey !== undefined, "No EncryptedKey is found");
    const keyAlgorithm = attributeOf(child(encryptedKey, "EncryptionMethod"), "Algorithm");
    const dataAlgorithm = attributeOf(child(encryptedData, "EncryptionMethod"), "Algorithm");
    const plaintext = decryptData(encryptedData, decryptKey(encryptedKey, privateKey));
    return {keyAlgorithm, dataAlgorithm, plaintext};
}

/*
 * Attributes keyed by friendly name: the default SAML2 attribute definitions release uid and mail under
 * their urn:oid names, with the friendly name carrying the plain one.
 */
function attributesOf(attributeStatement) {
    const attributes = {};
    children(attributeStatement, "Attribute").forEach((attribute) => {
        const name = attributeOf(attribute, "FriendlyName") ?? attributeOf(attribute, "Name");
        attributes[name] = children(attribute, "AttributeValue").map(textOf);
    });
    return attributes;
}

function assertSuccess(response) {
    const statusCode = child(child(response, "Status"), "StatusCode");
    assert.equal(attributeOf(statusCode, "Value"), "urn:oasis:names:tc:SAML:2.0:status:Success");
}

async function fetchSamlResponse(browser, entityId) {
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);
    try {
        const url = `https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO?providerId=${encodeURIComponent(entityId)}`;
        await cas.log(`Navigating to ${url}`);
        await cas.goto(page, url);
        await cas.sleep(2000);
        await cas.loginWith(page);
        await cas.sleep(3000);
        await cas.logPage(page);
        await cas.screenshot(page);
        const pageUrl = page.url();
        if (!pageUrl.startsWith("https://localhost:9859/")) {
            return {pageUrl};
        }
        const content = JSON.parse(await cas.innerText(page, "body"));
        assert(content.form.SAMLResponse !== undefined);
        const xml = await cas.base64Decode(content.form.SAMLResponse);
        await cas.log(xml);
        const response = child(await cas.parseXML(xml), "Response");
        assert(response !== undefined, "No SAML2 Response is found");
        return {pageUrl, response};
    } finally {
        await context.close();
    }
}

async function verifyEncryptedAssertion(browser, privateKey, entityId, expectedDataAlgorithm, expectedKeyAlgorithm) {
    await cas.log(`Checking encrypted assertion for ${entityId}`);
    const {response} = await fetchSamlResponse(browser, entityId);
    assertSuccess(response);
    assert.equal(children(response, "Assertion").length, 0, "Response must not carry a plain assertion");
    const encryptedAssertions = children(response, "EncryptedAssertion");
    assert.equal(encryptedAssertions.length, 1);

    const result = decryptElement(encryptedAssertions[0], privateKey);
    await cas.log(`Data encryption algorithm: ${result.dataAlgorithm}, key transport algorithm: ${result.keyAlgorithm}`);
    assert.equal(result.dataAlgorithm, expectedDataAlgorithm);
    assert.equal(result.keyAlgorithm, expectedKeyAlgorithm);

    const assertion = child(await cas.parseXML(result.plaintext), "Assertion");
    assert(assertion !== undefined, "Decrypted content is not an assertion");
    const attributes = attributesOf(child(assertion, "AttributeStatement"));
    await cas.log(attributes);
    assert.deepEqual(attributes.mail, ["casuser@example.org"]);
    assert.deepEqual(attributes.uid, ["casuser"]);
}

async function verifyEncryptableAttributes(browser, privateKey) {
    const entityId = `${ENTITY_PREFIX}/sp-attributes`;
    await cas.log(`Checking encryptable attributes for ${entityId}`);
    const {response} = await fetchSamlResponse(browser, entityId);
    assertSuccess(response);
    assert.equal(children(response, "EncryptedAssertion").length, 0);
    const assertion = child(response, "Assertion");
    assert(assertion !== undefined, "Response must carry a plain assertion");

    const attributeStatement = child(assertion, "AttributeStatement");
    const attributes = attributesOf(attributeStatement);
    await cas.log(attributes);
    assert.deepEqual(attributes.uid, ["casuser"]);
    assert(attributes.mail === undefined, "mail must only be released encrypted");

    const encryptedAttributes = children(attributeStatement, "EncryptedAttribute");
    assert.equal(encryptedAttributes.length, 1, "Only mail is listed as an encryptable attribute");
    const result = decryptElement(encryptedAttributes[0], privateKey);
    await cas.log(`Data encryption algorithm: ${result.dataAlgorithm}, key transport algorithm: ${result.keyAlgorithm}`);
    assert.equal(result.keyAlgorithm, `${XMLENC}rsa-oaep-mgf1p`);
    const attribute = child(await cas.parseXML(result.plaintext), "Attribute");
    assert.equal(attributeOf(attribute, "FriendlyName"), "mail");
    assert.equal(attributeOf(attribute, "Name"), "urn:oid:0.9.2342.19200300.100.1.3");
    assert.deepEqual(children(attribute, "AttributeValue").map(textOf), ["casuser@example.org"]);
}

async function verifyEncryptionOptional(browser) {
    const entityId = `${ENTITY_PREFIX}/sp-optional`;
    await cas.log(`Checking optional encryption for ${entityId}, whose metadata has no encryption key`);
    const {response} = await fetchSamlResponse(browser, entityId);
    assertSuccess(response);
    assert.equal(children(response, "EncryptedAssertion").length, 0);
    const assertion = child(response, "Assertion");
    assert(assertion !== undefined, "Response must carry a plain assertion when encryption is optional");
    assert.deepEqual(attributesOf(child(assertion, "AttributeStatement")).mail, ["casuser@example.org"]);
}

async function verifyEncryptionRequired(browser) {
    const entityId = `${ENTITY_PREFIX}/sp-required`;
    await cas.log(`Checking required encryption for ${entityId}, whose metadata has no encryption key`);
    const {pageUrl, response} = await fetchSamlResponse(browser, entityId);
    await cas.log(`Final page is ${pageUrl}`);
    assert(response === undefined, "No SAML2 response may reach the service provider without encryption");
    assert(pageUrl.startsWith("https://localhost:8443/cas/"));
}

async function verifySimpleSamlPhp(browser) {
    const page = await cas.newPage(browser);
    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await cas.sleep(2000);
    await cas.screenshot(page);
    await cas.loginWith(page);
    await cas.sleep(2000);
    await cas.screenshot(page);
    await page.waitForSelector("#table_with_attributes", {visible: true});
    await cas.assertInnerTextContains(page, "#content p", "status page of SimpleSAMLphp");
    await cas.assertVisibility(page, "#table_with_attributes");

    const authData = JSON.parse(await cas.innerHTML(page, "details pre"));
    await cas.log(authData);
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        await verifySimpleSamlPhp(browser);

        const privateKey = crypto.createPrivateKey(fs.readFileSync(path.join(__dirname, "sp-encryption.key")));
        await verifyEncryptedAssertion(browser, privateKey, `${ENTITY_PREFIX}/sp-gcm`,
            `${XMLENC11}aes256-gcm`, `${XMLENC}rsa-oaep-mgf1p`);
        await verifyEncryptedAssertion(browser, privateKey, `${ENTITY_PREFIX}/sp-cbc`,
            `${XMLENC}aes128-cbc`, `${XMLENC11}rsa-oaep`);
        await verifyEncryptableAttributes(browser, privateKey);
        await verifyEncryptionOptional(browser);
        await verifyEncryptionRequired(browser);

        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    } finally {
        await cas.closeBrowser(browser);
    }
})();
