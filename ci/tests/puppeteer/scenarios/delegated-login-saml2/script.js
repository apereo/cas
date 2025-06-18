
const cas = require("../../cas.js");
const path = require("path");
const assert = require("assert");
const https = require("https");
const service = "https://localhost:9859/anything/sample1";

async function verifyNormalAuthenticationFlow(browser) {
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await cas.sleep(2000);

    await cas.assertVisibility(page, "#loginProviders");
    await cas.assertVisibility(page, "li #SAML2Client");

    await cas.click(page, "li #SAML2Client");
    await cas.waitForNavigation(page);

    await cas.loginWith(page, "user1", "password");
    await cas.sleep(8000);
    await cas.screenshot(page);
    await cas.logPage(page);
    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
    await cas.assertCookie(page, true, "Pac4jCookie");
    await cas.screenshot(page);

    await cas.gotoLogin(page, service);
    const ticket = await cas.assertTicketParameter(page);
    const json = await cas.validateTicket(service, ticket);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.email[0] === "Hello-user1@example.com");

    await cas.log("Testing auto-redirection via configured cookie...");
    await cas.gotoLogout(page);
    await cas.sleep(3000);
    await cas.gotoLogin(page);
    await cas.sleep(2000);
    await cas.logPage(page);
    await cas.sleep(3000);
    await cas.assertPageUrlStartsWith(page, "http://localhost:9443/simplesaml/");
}

async function verifySaml1AuthenticationFlow(context) {
    const page = await cas.newPage(context);

    await cas.goto(page, `https://localhost:8443/cas/login?TARGET=${service}`);
    await cas.assertVisibility(page, "#loginProviders");
    await cas.assertVisibility(page, "li #SAML2Client");

    await cas.click(page, "li #SAML2Client");
    await cas.waitForNavigation(page);

    await cas.loginWith(page, "user1", "password");
    await cas.sleep(8000);
    await cas.screenshot(page);
    await cas.logPage(page);
    const ticket = await cas.assertParameter(page, "SAMLart");

    const request = `<?xml version="1.0" encoding="UTF-8"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
<SOAP-ENV:Header/>
<SOAP-ENV:Body>
<samlp:Request xmlns:samlp="urn:oasis:names:tc:SAML:1.0:protocol" MajorVersion="1"
MinorVersion="1" RequestID="_192.168.16.51.1024506224022"
IssueInstant="2021-06-19T17:03:44.022Z">
<samlp:AssertionArtifact>${ticket}</samlp:AssertionArtifact>
</samlp:Request>
</SOAP-ENV:Body>
</SOAP-ENV:Envelope>`;

    const options = {
        protocol: "https:",
        hostname: "localhost",
        port: 8443,
        path: `/cas/samlValidate?TARGET=${service}&SAMLart=${ticket}`,
        method: "POST",
        rejectUnauthorized: false,
        headers: {
            "Content-Length": request.length
        }
    };

    const post = (options) =>
        new Promise((resolve, reject) => {
            const req = https
                .request(options, (res) => {
                    res.setEncoding("utf8");
                    const body = [];
                    res.on("data", (chunk) => body.push(chunk));
                    res.on("end", () => resolve(body.join("")));
                })
                .on("error", reject);
            req.write(request);
        });
    const body = await post(options);
    await cas.log(body);
    assert(body.includes("<saml1:AttributeValue>Hello-user1@example.com</saml1:AttributeValue>"));
    assert(body.includes("<saml1:AttributeValue>SAML2Client</saml1:AttributeValue>"));
    assert(body.includes("<saml1:AttributeValue>urn:oasis:names:tc:SAML:2.0:ac:classes:Password</saml1:AttributeValue>"));
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());

    let context = await browser.createBrowserContext();
    await verifyNormalAuthenticationFlow(context);
    await context.close();

    context = await browser.createBrowserContext();
    await verifySaml1AuthenticationFlow(context);
    await context.close();
    
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await browser.close();
})();

