
const performance = require("perf_hooks").performance;
const cas = require("../../cas.js");
const path = require("path");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const entityIds = [
        "https://studypages.com/saml-sp",
        "https://aca.ucop.edu",
        "https://www.peoplegrove.com/saml",
        "https://uchicago.infoready4.com/shibboleth",
        "https://cole.uconline.edu/shibboleth-sp",
        "https://apps.universityrelations.cornell.edu/Shibboleth",
        "https://brand.cornell.edu/Shibboleth",
        "https://cac.cornell.edu/shibboleth",
        "https://downloads.cornell.edu/shibboleth",
        "https://analytics.uchealth.edu/sp",
        "https://appstream.ucop.edu",
        "https://osprey.dartmouth.edu/shibboleth",
        "https://auth.uconline.edu/shibboleth",
        "https://dartmouth.bioraft.com/shibboleth",
        "https://princeton.nupark.com/sp",
        "https://harvardhipaa.zoom.us",
        "https://harvard.zoom.us",
        "https://dataverse.harvard.edu/sp",
        "https://yale.peopleadmin.com/shibboleth"
    ];

    await sendRequest(page, entityIds);
    await cas.doDelete("https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadataCache");

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();

async function sendRequest(page, entityIds) {
    let count = 0;
    for (const entityId of entityIds) {
        try {
            await cas.gotoLogout(page);

            let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
            url += `?providerId=${entityId}`;
            url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";

            await cas.log(`Navigating to ${url}`);
            const s = performance.now();
            await cas.goto(page, url);
            const e = performance.now();
            const duration = (e - s) / 1000;
            await cas.log(`Request took ${duration} seconds.`);

            if (count > 1 && duration > 15) {
                await cas.logr("Request took longer than expected");
            }

            await cas.sleep(1000);
            await cas.assertVisibility(page, "#username");
            await cas.assertVisibility(page, "#password");
            await cas.loginWith(page);
            await cas.sleep(1000);
            count++;
        } catch (e) {
            await cas.logr(e);
        }
    }
}
