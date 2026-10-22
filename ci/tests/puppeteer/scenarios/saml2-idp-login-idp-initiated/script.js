
const path = require("path");
const assert = require("assert");
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

    await cas.separator();
    await cas.log("Unsolicited SSO must refuse an assertion consumer service that is not in the service provider metadata");
    const unregisteredAcs = "https://localhost:9859/anything/unregistered-acs";
    const refused = await cas.goto(page, `${url}&shire=${encodeURIComponent(unregisteredAcs)}`);
    await cas.sleep(2000);
    await cas.logPage(page);
    assert(refused.status() >= 400, `Unregistered ACS must be refused, but got ${refused.status()}`);
    assert(!page.url().startsWith("https://localhost:9859"), "No response may be delivered to an unregistered ACS");

    await cas.separator();
    await cas.log("Unsolicited SSO must accept the assertion consumer service listed in the service provider metadata");
    const registeredAcs = "http://localhost:9443/simplesaml/module.php/saml/sp/saml2-acs.php/default-sp";
    await cas.goto(page, `${url}&shire=${encodeURIComponent(registeredAcs)}`);
    await cas.sleep(4000);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.closeBrowser(browser);

    let endpoint = await getActuatorEndpoint(entityId);
    await cas.separator();
    await cas.log(`Trying ${endpoint} via POST`);
    await cas.log(await cas.doRequest(endpoint, "POST", {}, 200));
    await cas.separator();
    endpoint = await getActuatorEndpoint(entityId, "");
    await cas.log(`Trying ${endpoint} via POST without password`);
    await cas.log(await cas.doRequest(endpoint, "POST", {}, 200));

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
})();

