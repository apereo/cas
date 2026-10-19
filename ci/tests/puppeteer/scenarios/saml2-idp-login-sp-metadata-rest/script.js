const assert = require("assert");
const path = require("path");
const express = require("express");
const cas = require("../../cas.js");

const ENTITY_ID = "http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp";
const MANAGERS_URL = "https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadata/managers";
const MANAGER_URL = `${MANAGERS_URL}/RestfulSamlRegisteredServiceMetadataResolver`;
const JSON_HEADERS = {"Accept": "application/json", "Content-Type": "application/json"};

(async () => {
    const documents = new Map();
    const lookups = [];
    let nextId = 1;
    const app = express();
    app.use(express.text({type: "*/*", limit: "5mb"}));
    app.get("/saml", (req, res) => {
        const {entityId, id, name} = req.query;
        const all = [...documents.values()];
        if (entityId !== undefined) {
            lookups.push(entityId);
            const found = all.find((doc) => doc.entityId === entityId || doc.value.includes(`entityID="${entityId}"`));
            return found === undefined ? res.sendStatus(404) : res.json(found);
        }
        if (id !== undefined || name !== undefined) {
            const found = all.find((doc) => String(doc.id) === id || doc.name === name);
            return found === undefined ? res.sendStatus(404) : res.json(found);
        }
        return res.json(all);
    });
    app.post("/saml", (req, res) => {
        const doc = JSON.parse(req.body);
        doc.id = doc.id > 0 ? doc.id : nextId++;
        cas.log(`Storing metadata document ${doc.name} as ${doc.id}`);
        documents.set(String(doc.id), doc);
        res.sendStatus(200);
    });
    app.delete("/saml", (req, res) => {
        const {id, name} = req.query;
        for (const [key, doc] of documents) {
            if ((id === undefined && name === undefined) || key === id || doc.name === name) {
                documents.delete(key);
            }
        }
        res.sendStatus(200);
    });
    const server = await new Promise((resolve) => {
        const started = app.listen(5617, () => resolve(started));
    });

    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        await cas.logg("Service provider metadata uploaded through CAS lands in the REST backend");
        const xml = await cas.doRequest(ENTITY_ID, "GET", {}, 200);
        assert(xml.includes(ENTITY_ID));
        await cas.doRequest(MANAGER_URL, "POST", JSON_HEADERS, 200, JSON.stringify({name: "SimpleSAMLphp", value: xml}));
        assert(documents.size === 1);
        const stored = [...documents.values()][0];
        assert(stored.name === "SimpleSAMLphp" && stored.value === xml);

        await cas.logg("The REST metadata manager is listed and lists the documents the backend holds");
        const managers = JSON.parse(await cas.doRequest(MANAGERS_URL, "GET", {"Accept": "application/json"}));
        const manager = managers.find((entry) => entry.name === "RestfulSamlRegisteredServiceMetadataResolver");
        assert(manager !== undefined && manager.sourceId === "rest://");
        const listed = JSON.parse(await cas.doRequest(MANAGER_URL, "GET", {"Accept": "application/json"}));
        assert(listed.length === 1 && listed[0].name === "SimpleSAMLphp");
        const found = JSON.parse(await cas.doRequest(`${MANAGER_URL}/${stored.id}`, "GET", {"Accept": "application/json"}));
        assert(found.value === xml);

        await cas.logg("Login resolves the service provider metadata from the REST backend");
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
        assert(lookups.includes(ENTITY_ID), "The metadata must be looked up by entity id");

        await cas.logg("Removing the document through CAS removes it from the REST backend");
        await cas.doRequest(`${MANAGER_URL}/${stored.id}`, "DELETE", {"Content-Type": "application/x-www-form-urlencoded"}, 204);
        assert(documents.size === 0);
    } finally {
        await cas.closeBrowser(browser);
        server.close();
        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    }
})();
