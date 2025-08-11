
const path = require("path");
const cas = require("../../cas.js");

async function getActuatorEndpoint(entityId, password = "Mellon") {
    const baseEndpoint = "https://localhost:8443/cas/actuator/samlPostProfileResponse";
    let actuator = `${baseEndpoint}?username=casuser&entityId=${entityId}&encrypt=false`;
    if (password !== undefined && password !== "") {
        actuator = `${actuator}&password=${password}`;
    }
    return actuator;
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const entityId = "http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp";
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}`;
    url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";
    await cas.log(`Navigating to ${url}`);
    await cas.goto(page, url);
    await cas.screenshot(page);
    await cas.sleep(4000);
    await cas.loginWith(page);
    await cas.sleep(4000);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.closeBrowser(browser);

    let endpoint = await getActuatorEndpoint(entityId);
    await cas.log("===================================");
    await cas.log(`Trying ${endpoint} via POST`);
    await cas.log(await cas.doRequest(endpoint, "POST", {}, 200));
    await cas.log("===================================");
    endpoint = await getActuatorEndpoint(entityId, "");
    await cas.log(`Trying ${endpoint} via POST without password`);
    await cas.log(await cas.doRequest(endpoint, "POST", {}, 200));

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
})();

