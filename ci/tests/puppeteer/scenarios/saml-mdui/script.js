
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.logg("Testing static metadata for MDUI");
    await runTestsFor(page, "https://carmenwiki.osu.edu/shibboleth");

    await cas.logg("Testing dynamic metadata (MDQ) for MDUI");
    await runTestsFor(page, "urn:mace:incommon:uchicago.edu:mdupload", false);
    await runTestsFor(page, "http://unicon.instructure.com/saml2");
    await cas.closeBrowser(browser);
})();

async function runTestsFor(page, entityId, hasInfoUrl = true) {
    const service = encodeURIComponent(`https://apereo.github.io?entityId=${entityId}`);
    await cas.log(`Using service ${service}`);
    await cas.gotoLogin(page, service);
    await cas.sleep(1000);
    await verify(page, hasInfoUrl);

    const url = `https://localhost:8443/cas/login?entityId=${encodeURIComponent(entityId)}&service=https://apereo.github.io`;
    await cas.log(`Using URL ${url}`);
    await cas.goto(page, url);
    await cas.sleep(1000);
    await verify(page, hasInfoUrl);
}

async function verify(page, hasInfoUrl) {
    await cas.assertVisibility(page, "#serviceUIMetadataLogo");
    await cas.assertVisibility(page, "#serviceUIMetadataDisplayName");
    await cas.assertVisibility(page, "#serviceUIMetadataDescription");
    if (hasInfoUrl) {
        await cas.assertVisibility(page, "#serviceUIMetadataInformationUrl");
    }
    await cas.assertVisibility(page, "#serviceUIMetadataPrivacyUrl");
}
