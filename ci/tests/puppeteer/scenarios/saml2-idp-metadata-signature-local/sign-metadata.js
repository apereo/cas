const fs = require("fs");
const path = require("path");
const crypto = require("crypto");

/*
 * Writes the service provider metadata this scenario trusts or refuses: one aggregate (EntitiesDescriptor),
 * signed with signer.key, a copy with one byte changed after signing, and an unsigned copy.
 *
 * The aggregate is written in its exclusive canonical form (attributes in canonical order, namespaces declared
 * where first used, no self-closing elements), so the digest of these exact bytes is the digest of the root
 * element once the enveloped signature is removed, and the same holds for SignedInfo. The signature is the
 * first child of the root, where the EntitiesDescriptor schema places it.
 *
 * Usage: node sign-metadata.js <directory holding signer.key, which also receives the metadata>
 */
const MD = "urn:oasis:names:tc:SAML:2.0:metadata";
const DS = "http://www.w3.org/2000/09/xmldsig#";
const EXC_C14N = "http://www.w3.org/2001/10/xml-exc-c14n#";
const AGGREGATE_ID = "_cas-puppeteer-metadata-aggregate";
const AGGREGATE_NAME = "urn:apereo:cas:puppeteer:metadata-aggregate";

const ENTITY_IDS = [
    "https://localhost:9859/sig-trusted",
    "https://localhost:9859/sig-wrong-cert",
    "https://localhost:9859/sig-tampered",
    "https://localhost:9859/sig-unsigned",
    "https://localhost:9859/sig-unsigned-optional"
];

function entityDescriptor(entityId) {
    return `<EntityDescriptor entityID="${entityId}">`
        + "<SPSSODescriptor AuthnRequestsSigned=\"false\" WantAssertionsSigned=\"false\" "
        + "protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">"
        + "<NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</NameIDFormat>"
        + "<AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" "
        + "Location=\"https://localhost:9859/post\" index=\"0\"></AssertionConsumerService>"
        + "</SPSSODescriptor></EntityDescriptor>";
}

function aggregate() {
    const entities = ENTITY_IDS.map(entityDescriptor).join("\n");
    return `<EntitiesDescriptor xmlns="${MD}" ID="${AGGREGATE_ID}" Name="${AGGREGATE_NAME}">\n${entities}\n</EntitiesDescriptor>`;
}

function sign(unsigned, privateKey) {
    const digest = crypto.createHash("sha256").update(unsigned, "utf8").digest("base64");
    const signedInfo = `<ds:SignedInfo xmlns:ds="${DS}">`
        + `<ds:CanonicalizationMethod Algorithm="${EXC_C14N}"></ds:CanonicalizationMethod>`
        + "<ds:SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"></ds:SignatureMethod>"
        + `<ds:Reference URI="#${AGGREGATE_ID}"><ds:Transforms>`
        + "<ds:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"></ds:Transform>"
        + `<ds:Transform Algorithm="${EXC_C14N}"></ds:Transform></ds:Transforms>`
        + "<ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"></ds:DigestMethod>"
        + `<ds:DigestValue>${digest}</ds:DigestValue></ds:Reference></ds:SignedInfo>`;
    const signatureValue = crypto.sign("sha256", Buffer.from(signedInfo, "utf8"), privateKey).toString("base64");
    const signature = `<ds:Signature xmlns:ds="${DS}">${signedInfo}<ds:SignatureValue>${signatureValue}</ds:SignatureValue></ds:Signature>`;
    const index = unsigned.indexOf(">") + 1;
    return unsigned.substring(0, index) + signature + unsigned.substring(index);
}

const directory = process.argv[2];
if (directory === undefined) {
    console.error("Usage: node sign-metadata.js <directory>");
    process.exit(1);
}
const privateKey = crypto.createPrivateKey(fs.readFileSync(path.join(directory, "signer.key"), "utf8"));
const unsigned = aggregate();
const signed = sign(unsigned, privateKey);
const tampered = signed.replace(`Name="${AGGREGATE_NAME}"`, `Name="${AGGREGATE_NAME.slice(0, -1)}X"`);
if (tampered === signed) {
    console.error("Unable to tamper with the signed metadata");
    process.exit(1);
}
const declaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
fs.writeFileSync(path.join(directory, "aggregate-signed.xml"), declaration + signed);
fs.writeFileSync(path.join(directory, "aggregate-tampered.xml"), declaration + tampered);
fs.writeFileSync(path.join(directory, "aggregate-unsigned.xml"), declaration + unsigned);
console.log(`Signed, tampered and unsigned metadata aggregates are written to ${directory}`);
