const assert = require("assert");
const path = require("path");
const {execFileSync} = require("child_process");
const cas = require("../../cas.js");

const ENTITY_ID = "http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp";
const MANAGERS_URL = "https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadata/managers";
const MANAGER_URL = `${MANAGERS_URL}/RedisSamlRegisteredServiceMetadataResolver`;

async function redisKeys(pattern) {
    const output = execFileSync("docker", ["exec", "redis_server_master", "redis-cli", "--scan", "--pattern", pattern]).toString().trim();
    await cas.log(`Redis keys matching ${pattern}:\n${output}`);
    return output.length === 0 ? [] : output.split("\n");
}

async function metadataDocuments() {
    return JSON.parse(await cas.doRequest(MANAGER_URL, "GET", {"Accept": "application/json"}));
}

(async () => {
    await cas.logg("Identity provider and service provider metadata are kept in Redis");
    assert((await redisKeys("SamlIdPMetadataDocument:*")).length > 0);
    assert((await redisKeys("SamlMetadataDocument:*")).length === 1);
    const documents = await metadataDocuments();
    assert(documents.length === 1);
    assert(documents[0].name === "SimpleSAMLphp");
    assert(documents[0].entityId === ENTITY_ID);

    await cas.logg("The Redis metadata manager is listed, and documents can be fetched and replaced by id");
    const managers = JSON.parse(await cas.doRequest(MANAGERS_URL, "GET", {"Accept": "application/json"}));
    const manager = managers.find((entry) => entry.name === "RedisSamlRegisteredServiceMetadataResolver");
    assert(manager !== undefined && manager.sourceId === "redis://");
    const stored = JSON.parse(await cas.doRequest(`${MANAGER_URL}/${documents[0].id}`, "GET", {"Accept": "application/json"}));
    assert(stored.entityId === ENTITY_ID && stored.value === documents[0].value);
    const updated = JSON.parse(await cas.doRequest(MANAGER_URL, "PUT",
        {"Content-Type": "application/json", "Accept": "application/json"}, 200,
        JSON.stringify({id: stored.id, name: stored.name, value: stored.value})));
    assert(updated.id === stored.id && updated.entityId === ENTITY_ID);
    assert((await redisKeys("SamlMetadataDocument:*")).length === 1);
    assert((await metadataDocuments()).length === 1);
    await cas.doRequest(`${MANAGER_URL}/987654321`, "GET", {"Accept": "application/json"}, 404);
    await cas.doRequest(`${MANAGERS_URL}/UnknownManager`, "GET", {"Accept": "application/json"}, 404);

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

    await cas.logg("Removing the service provider metadata document removes it from Redis");
    await cas.doRequest(`${MANAGER_URL}/${documents[0].id}`, "DELETE",
        {"Content-Type": "application/x-www-form-urlencoded"}, 204);
    assert((await redisKeys("SamlMetadataDocument:*")).length === 0);
    assert((await metadataDocuments()).length === 0);
    assert((await redisKeys("SamlIdPMetadataDocument:*")).length > 0);
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
})();
