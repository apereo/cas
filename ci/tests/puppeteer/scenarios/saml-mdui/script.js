const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.logg("Testing static metadata for MDUI")
    await runTestsFor(page, "https://carmenwiki.osu.edu/shibboleth")

    await cas.logg("Testing dynamic metadata (MDQ) for MDUI")
    await runTestsFor(page, "urn:mace:incommon:uchicago.edu:mdupload", false)
    await runTestsFor(page, "http://unicon.instructure.com/saml2")
    await browser.close();
})();

async function runTestsFor(page, entityId, hasInfoUrl = true) {
    let service = encodeURIComponent(`https://apereo.github.io?entityId=${entityId}`);
    console.log(`Using service ${service}`)
    await page.goto(`https://localhost:8443/cas/login?service=${service}`);
    await page.waitForTimeout(1000)
    await verify(page, hasInfoUrl);

    let url = `https://localhost:8443/cas/login?entityId=${encodeURIComponent(entityId)}&service=https://apereo.github.io`;
    console.log(`Using URL ${url}`)
    await page.goto(url);
    await page.waitForTimeout(1000)
    await verify(page, hasInfoUrl);
}

async function verify(page, hasInfoUrl) {
    await cas.assertVisibility(page, "#serviceUIMetadataLogo")
    await cas.assertVisibility(page, "#serviceUIMetadataDisplayName")
    await cas.assertVisibility(page, "#serviceUIMetadataDescription")
    if (hasInfoUrl) {
        await cas.assertVisibility(page, "#serviceUIMetadataInformationUrl")
    }
    await cas.assertVisibility(page, "#serviceUIMetadataPrivacyUrl")
}
