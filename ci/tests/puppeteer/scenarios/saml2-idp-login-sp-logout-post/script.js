const cas = require("../../cas.js");
const path = require("path");
const fs = require("fs");
const os = require("os");

async function getActuatorEndpoint(entityId) {
    const baseEndpoint = "https://localhost:8443/cas/actuator/samlPostProfileResponse/logout/post";
    return `${baseEndpoint}?entityId=${entityId}`;
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);
        const service = "https://localhost:9859/anything/cas";
        await cas.gotoLogin(page, service);
        await cas.sleep(1000);
        await cas.loginWith(page);

        const ticket = await cas.assertTicketParameter(page);
        await cas.doRequest(`https://localhost:8443/cas/validate?service=${service}&ticket=${ticket}`);

        const endpoint = await getActuatorEndpoint("http://localhost:9443/simplesaml/module.php/saml/sp/metadata.php/default-sp");
        const sloPage = await cas.doPost(endpoint, {}, {
            "Content-Type": "application/json"
        }, (res) => res.data, (error) => {
            throw (error);
        });
        const tempDir = os.tmpdir();
        const sloFile = `${tempDir}/saml2slo.html`;
        await fs.writeFileSync(sloFile, sloPage);
        await cas.log(`Logout page is written to ${sloFile}`);

        await cas.goto(page, `file://${sloFile}`);
        await cas.sleep(4000);
        await cas.logPage(page);
        await cas.assertVisibility(page, "#logoutPostButton");
        await cas.submitForm(page, "#logoutPostButton");
        await cas.sleep(2000);
        await cas.assertPageUrl(page, "http://localhost:9443/simplesaml/module.php/saml/sp/saml2-logout.php/default-sp");
        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    } finally {
        await cas.closeBrowser(browser);
    }
})();
