const assert = require("assert");
const https = require("https");
const path = require("path");
const cas = require("../../cas.js");

const service = "https://localhost:9859/anything/stateless";

async function assertStatelessSession(page) {
    const tgc = await cas.assertCookie(page);
    await cas.log(`Ticket-granting cookie is ${tgc.value.length} characters`);
    assert(tgc.value.length < 4096, `Ticket-granting cookie is ${tgc.value.length} characters`);
    const oversized = (await page.cookies("https://localhost:8443/cas/login"))
        .filter((cookie) => cookie.name.length + cookie.value.length >= 4096);
    assert(oversized.length === 0, `Oversized cookies: ${oversized.map((cookie) => cookie.name)}`);
}

async function validate(ticket, clientName) {
    const json = await cas.validateTicket(service, ticket);
    const success = json.serviceResponse.authenticationSuccess;
    assert(success !== undefined, JSON.stringify(json));
    assert(success.attributes.clientName[0] === clientName, JSON.stringify(success.attributes));
    assert(success.attributes.department[0] === "engineering", JSON.stringify(success.attributes));
    return success;
}

async function verifyCasClient(browser) {
    const context = await browser.createBrowserContext();
    try {
        const page = await cas.newPage(context);
        await cas.gotoLogin(page, service);
        await cas.sleep(2000);
        await cas.assertVisibility(page, "li #CasClient");
        await cas.click(page, "li #CasClient");
        await cas.waitForNavigation(page);
        await cas.sleep(2000);
        await cas.loginWith(page);
        await cas.sleep(5000);
        await cas.logPage(page);
        await cas.assertPageUrlStartsWith(page, service);
        const success = await validate(await cas.assertTicketParameter(page), "CasClient");
        assert(success.user === "casuser", success.user);

        await cas.log("Single sign-on session from the delegated login is kept in the ticket-granting cookie");
        await cas.gotoLogin(page);
        await cas.sleep(1000);
        await assertStatelessSession(page);
        await cas.gotoLogin(page, service);
        await cas.sleep(2000);
        await validate(await cas.assertTicketParameter(page), "CasClient");
    } finally {
        await context.close();
    }
}

async function verifySaml2Client(browser) {
    const context = await browser.createBrowserContext();
    try {
        const page = await cas.newPage(context);
        const relayStates = [];
        page.on("request", (request) => {
            const url = new URL(request.url());
            if (url.pathname.endsWith("/SSOService.php") && url.searchParams.has("RelayState")) {
                relayStates.push(url.searchParams.get("RelayState"));
            }
        });
        await cas.gotoLogin(page, service);
        await cas.sleep(2000);
        await cas.assertVisibility(page, "li #SAML2Client");
        await cas.click(page, "li #SAML2Client");
        await cas.waitForNavigation(page);
        assert(relayStates.length > 0, "No RelayState was sent to the identity provider");
        await cas.log(`RelayState sent to the identity provider is ${relayStates[0].length} characters`);
        assert(relayStates[0].startsWith("TST-"), relayStates[0]);
        await cas.loginWith(page, "user1", "password");
        await cas.sleep(8000);
        await cas.logPage(page);
        await cas.assertPageUrlStartsWith(page, service);
        await validate(await cas.assertTicketParameter(page), "SAML2Client");

        await cas.gotoLogin(page);
        await cas.sleep(1000);
        await assertStatelessSession(page);
        await cas.gotoLogin(page, service);
        await cas.sleep(2000);
        await validate(await cas.assertTicketParameter(page), "SAML2Client");
    } finally {
        await context.close();
    }
}

async function verifySaml1Validation(browser) {
    const context = await browser.createBrowserContext();
    try {
        const page = await cas.newPage(context);
        await cas.goto(page, `https://localhost:8443/cas/login?TARGET=${service}`);
        await cas.sleep(2000);
        await cas.click(page, "li #SAML2Client");
        await cas.waitForNavigation(page);
        await cas.loginWith(page, "user1", "password");
        await cas.sleep(8000);
        const artifact = await cas.assertParameter(page, "SAMLart");
        await cas.log(`SAML1 artifact is ${artifact.length} characters`);
        const request = `<?xml version="1.0" encoding="UTF-8"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
<SOAP-ENV:Header/>
<SOAP-ENV:Body>
<samlp:Request xmlns:samlp="urn:oasis:names:tc:SAML:1.0:protocol" MajorVersion="1"
MinorVersion="1" RequestID="_192.168.16.51.1024506224022"
IssueInstant="2021-06-19T17:03:44.022Z">
<samlp:AssertionArtifact>${artifact}</samlp:AssertionArtifact>
</samlp:Request>
</SOAP-ENV:Body>
</SOAP-ENV:Envelope>`;
        const body = await new Promise((resolve, reject) => {
            const req = https.request({
                protocol: "https:",
                hostname: "localhost",
                port: 8443,
                path: `/cas/samlValidate?TARGET=${service}&SAMLart=${encodeURIComponent(artifact)}`,
                method: "POST",
                rejectUnauthorized: false,
                headers: {
                    "Content-Length": Buffer.byteLength(request)
                }
            }, (res) => {
                res.setEncoding("utf8");
                const chunks = [];
                res.on("data", (chunk) => chunks.push(chunk));
                res.on("end", () => resolve(chunks.join("")));
            }).on("error", reject);
            req.write(request);
            req.end();
        });
        await cas.log(body);
        assert(body.includes("<saml1:AttributeValue>engineering</saml1:AttributeValue>"));
        assert(body.includes("<saml1:AttributeValue>SAML2Client</saml1:AttributeValue>"));
    } finally {
        await context.close();
    }
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    let failed = false;
    try {
        await verifyCasClient(browser);
        await cas.separator();
        await verifySaml2Client(browser);
        await cas.separator();
        await verifySaml1Validation(browser);
    } catch (e) {
        failed = true;
        throw e;
    } finally {
        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
        await cas.closeBrowser(browser);
        if (!failed) {
            await process.exit(0);
        }
    }
})();
