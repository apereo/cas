const puppeteer = require('puppeteer');
const performance = require('perf_hooks').performance;
const cas = require('../../cas.js');
const path = require("path");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    const entityIds = [
        "https://studypages.com/saml-sp",
        "https://aca.ucop.edu",
        "https://www.peoplegrove.com/saml",
        "https://login.at.internet2.edu/Saml2/proxy_saml2_backend.xml",
        "https://uchicago.infoready4.com/shibboleth",
        "https://cole.uconline.edu/shibboleth-sp",
        "https://apps.universityrelations.cornell.edu/Shibboleth",
        "https://brand.cornell.edu/Shibboleth",
        "https://cac.cornell.edu/shibboleth",
        "https://downloads.cornell.edu/shibboleth",
        "https://analytics.uchealth.edu/sp",
        "https://appstream.ucop.edu",
        "https://osprey.dartmouth.edu/shibboleth",
        "https://fourier.dartmouth.edu/shibboleth",
        "https://auth.uconline.edu/shibboleth",
        "https://dartmouth.bioraft.com/shibboleth",
        "https://princeton.bioraft.com/shibboleth",
        "http://princeton.imodules.com/sp",
        "https://princeton.nupark.com/sp",
        "https://harvard.starrezhousing.com/StarRezPortal/",
        "https://harvardhipaa.zoom.us",
        "https://harvard.zoom.us",
        "https://dataverse.harvard.edu/sp",
        "https://yale.peopleadmin.com/shibboleth",
        "https://yale.campus.auth.edublogs.org/shibboleth"
    ];

    await sendRequest(page, entityIds);
    await cas.doRequest("https://localhost:8443/cas/actuator/samlIdPRegisteredServiceMetadataCache", "DELETE", {}, 204);

    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();

async function sendRequest(page, entityIds) {
    let count = 0;
    for (const entityId of entityIds) {
        await page.goto("https://localhost:8443/cas/logout");

        let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
        url += `?providerId=${entityId}`;
        url += "&target=https%3A%2F%2Flocalhost%3A8443%2Fcas%2Flogin";

        console.log(`Navigating to ${url}`);
        let s = performance.now();
        await page.goto(url);
        let e = performance.now();
        let duration = (e - s) / 1000;
        console.log(`Request took ${duration} seconds.`)

        if (count > 1 && duration > 15) {
            throw "Request took longer than expected";
        }

        await page.waitForTimeout(1000);
        await cas.assertVisibility(page, '#username')
        await cas.assertVisibility(page, '#password')
        await cas.loginWith(page, "casuser", "Mellon");
        count++;
    }
}
