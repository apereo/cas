const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require("assert");
const path = require("path");

async function startFlow(context, clientName) {
    const page = await cas.newPage(context);
    const entityId = encodeURI("https://localhost:9859/shibboleth");
    let url = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
    url += `?providerId=${entityId}&CName=${clientName}`;
    await cas.log(`Navigating to ${url} for client ${clientName}`);
    await cas.goto(page, url);
    await page.waitForTimeout(3000);
    await cas.loginWith(page);
    await cas.logPage(page);
    await cas.screenshot(page);
    await cas.waitForElement(page, "body");
    const content = JSON.parse(await cas.innerText(page, "body"));
    await cas.log(content);
    assert(content.form.SAMLResponse != null);
    let samlResponse = await cas.base64Decode(content.form.SAMLResponse);
    let parsedResult = await cas.parseXML(samlResponse);
    console.dir(parsedResult, {depth: null, colors: true});
    assert(parsedResult["saml2p:Response"]["$"]["InResponseTo"] === undefined);
    const subjectConfirmation = parsedResult["saml2p:Response"]["saml2:Assertion"][0]["saml2:Subject"][0]["saml2:SubjectConfirmation"][0];
    assert(subjectConfirmation["saml2:SubjectConfirmationData"][0]["$"]["InResponseTo"] === undefined);
    assert(subjectConfirmation["saml2:SubjectConfirmationData"][0]["$"]["Address"] === "127.0.0.1");

    await cas.gotoLogin(page, "https://apereo.github.io");
    await page.waitForTimeout(6000);
    await cas.logPage(page);
    await cas.assertTicketParameter(page);
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const providers = ["CasClient", "CasClientFancy", "CasClientNone"];
    for (const provider of providers) {
        const context = await browser.createIncognitoBrowserContext();
        await startFlow(context, provider);
        await context.close();
    }
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
    await browser.close();
})();


