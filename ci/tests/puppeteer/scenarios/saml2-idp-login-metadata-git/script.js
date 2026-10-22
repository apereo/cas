const assert = require("assert");
const path = require("path");
const {execFileSync} = require("child_process");
const cas = require("../../cas.js");

const ROOT = "/tmp/cas-saml-metadata-git";
const ENTITY_ID = "http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp";
const MANAGER_URL = "https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadata/managers/GitSamlRegisteredServiceMetadataResolver";

async function remoteFiles() {
    const output = execFileSync("git", ["--git-dir", `${ROOT}/remote.git`, "ls-tree", "-r", "--name-only", "master"]).toString().trim();
    await cas.log(`Files in the remote repository:\n${output}`);
    return output.split("\n");
}

async function metadataDocuments() {
    return JSON.parse(await cas.doRequest(MANAGER_URL, "GET", {"Accept": "application/json"}));
}

(async () => {
    await cas.logg("CAS pushed its identity provider metadata to the remote repository");
    let files = await remoteFiles();
    assert(files.some((file) => file.endsWith("idp-metadata.xml")));
    assert(files.includes("sp-metadata/SimpleSAMLphp.xml"));

    await cas.logg("Service provider metadata committed to the remote by hand is pulled and served");
    const documents = await metadataDocuments();
    assert(documents.length === 1);
    assert(documents[0].name === "SimpleSAMLphp");
    const document = JSON.parse(await cas.doRequest(`${MANAGER_URL}/${documents[0].id}`, "GET", {"Accept": "application/json"}));
    assert(document.name === "SimpleSAMLphp");

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

    await cas.logg("Removing the service provider metadata document removes it from the remote repository");
    await cas.doRequest(`${MANAGER_URL}/${documents[0].id}`, "DELETE",
        {"Content-Type": "application/x-www-form-urlencoded"}, 204);
    assert((await metadataDocuments()).length === 0);
    files = await remoteFiles();
    assert(!files.includes("sp-metadata/SimpleSAMLphp.xml"));
    assert(files.some((file) => file.endsWith("idp-metadata.xml")));

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.removeDirectoryOrFile(ROOT);
})();
