
const assert = require("assert");
const https = require("https");
const cas = require("../../cas.js");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://apereo.github.io";

    await cas.goto(page, `https://localhost:8443/cas/login?TARGET=${service}`);
    await cas.loginWith(page);

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
    assert(body.includes("<saml1:NameIdentifier>casuser</saml1:NameIdentifier>"));
    assert(body.includes("<saml1:AttributeValue>Static Credentials</saml1:AttributeValue>"));
    assert(body.includes("<saml1:AttributeValue>urn:oasis:names:tc:SAML:1.0:am:password</saml1:AttributeValue>"));
    await cas.closeBrowser(browser);
})();
