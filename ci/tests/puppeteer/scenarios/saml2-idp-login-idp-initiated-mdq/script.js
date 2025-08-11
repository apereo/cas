
const performance = require("perf_hooks").performance;
const cas = require("../../cas.js");
const path = require("path");

async function tryServiceProviders(entityIds, page, timeout) {
    let count = 0;
    for (const entityId of entityIds) {
        await cas.log(`Trying service provider ${entityId} with timeout ${timeout}`);

        let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
        url += `?providerId=${entityId}`;
        url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";

        await cas.log(`Navigating to ${url}`);
        const s = performance.now();
        await cas.goto(page, url);
        await cas.sleep(2000);
        await cas.screenshot(page);
        const e = performance.now();
        const duration = (e - s) / 1000;
        await cas.log(`Request took ${duration} seconds for ${entityId}`);

        if (count > 1 && duration > duration) {
            throw `Request took longer than expected:${duration}`;
        }

        await cas.sleep(2000);
        await cas.assertVisibility(page, "#username");
        await cas.assertVisibility(page, "#password");
        await cas.log("=====================================");
        count++;
    }
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    const entityIds = [
        "https://studypages.com/saml-sp",
        "https://aca.ucop.edu",
        "https://www.peoplegrove.com/saml",
        "https://login.at.internet2.edu/Saml2/proxy_saml2_backend.xml",
        "https://uchicago.infoready4.com/shibboleth",
        "https://cole.uconline.edu/shibboleth-sp"
    ];

    await tryServiceProviders(entityIds, page, 15);
    await tryServiceProviders(entityIds, page, 5);

    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.closeBrowser(browser);
})();

