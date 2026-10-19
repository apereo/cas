const assert = require("assert");
const path = require("path");
const cas = require("../../cas.js");

const ENTITY_ID = "http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp";
const MANAGERS_URL = "https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadata/managers";
const MANAGER_URL = `${MANAGERS_URL}/DynamoDbSamlRegisteredServiceMetadataResolver`;
const SP_TABLE = "DynamoDbCasSamlMetadata";
const IDP_TABLE = "DynamoDbCasSamlIdPMetadata";

async function metadataDocuments() {
    return JSON.parse(await cas.doRequest(MANAGER_URL, "GET", {"Accept": "application/json"}));
}

(async () => {
    await cas.logg("Identity provider and service provider metadata are kept in DynamoDb");
    const idpItems = await cas.dynamoDbScan(IDP_TABLE);
    assert(idpItems.length > 0, "The identity provider metadata must be kept in DynamoDb");
    assert(idpItems.every((item) => item.metadata !== undefined && item.signingCertificate !== undefined));
    const spItems = await cas.dynamoDbScan(SP_TABLE);
    assert(spItems.length === 1);
    assert(spItems[0].entityId.S === ENTITY_ID);
    const documents = await metadataDocuments();
    assert(documents.length === 1);
    assert(documents[0].name === "SimpleSAMLphp");
    assert(documents[0].entityId === ENTITY_ID);

    await cas.logg("The DynamoDb metadata manager is listed, and documents can be fetched and replaced by id");
    const managers = JSON.parse(await cas.doRequest(MANAGERS_URL, "GET", {"Accept": "application/json"}));
    const manager = managers.find((entry) => entry.name === "DynamoDbSamlRegisteredServiceMetadataResolver");
    assert(manager !== undefined && manager.sourceId === "dynamodb://");
    const stored = JSON.parse(await cas.doRequest(`${MANAGER_URL}/${documents[0].id}`, "GET", {"Accept": "application/json"}));
    assert(stored.entityId === ENTITY_ID && stored.value === documents[0].value);
    const updated = JSON.parse(await cas.doRequest(MANAGER_URL, "PUT",
        {"Content-Type": "application/json", "Accept": "application/json"}, 200,
        JSON.stringify({id: stored.id, name: stored.name, value: stored.value})));
    assert(updated.id === stored.id && updated.entityId === ENTITY_ID);
    assert((await cas.dynamoDbScan(SP_TABLE)).length === 1);
    assert((await metadataDocuments()).length === 1);
    await cas.doRequest(`${MANAGER_URL}/987654321`, "GET", {"Accept": "application/json"}, 404);

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
        url += `?providerId=${ENTITY_ID}`;
        url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";
        await cas.goto(page, url);
        await cas.sleep(3000);
        await cas.loginWith(page);
        await cas.sleep(3000);
        await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    } finally {
        await cas.closeBrowser(browser);
    }

    await cas.logg("Removing the service provider metadata document removes it from DynamoDb");
    await cas.doRequest(`${MANAGER_URL}/${documents[0].id}`, "DELETE",
        {"Content-Type": "application/x-www-form-urlencoded"}, 204);
    assert((await cas.dynamoDbScan(SP_TABLE)).length === 0);
    assert((await metadataDocuments()).length === 0);
    assert((await cas.dynamoDbScan(IDP_TABLE)).length > 0);
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
})();
