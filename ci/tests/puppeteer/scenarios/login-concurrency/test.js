
const cas = require("../../cas.js");
const https = require("https");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const service = "https://localhost:9859/anything/app";
    let ticket;
    let userid;
    try {
        const page = await cas.newPage(browser);
        const availableUsers = ["casuser", "casadmin"];
        userid = availableUsers[Math.floor(Math.random() * availableUsers.length)];
        await cas.logb(`Selected user: ${userid}`);
        await cas.goto(page, `https://localhost:8443/cas/login?TARGET=${service}`);
        await cas.loginWith(page, userid, "Mellon");
        ticket = await cas.assertParameter(page, "SAMLart");
    } finally {
        await browser.close();
    }

    const request = `<?xml version="1.0" encoding="UTF-8"?>
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
<SOAP-ENV:Header/>
<SOAP-ENV:Body>
<samlp:Request xmlns:samlp="urn:oasis:names:tc:SAML:1.0:protocol" MajorVersion="1"
MinorVersion="1" RequestID="_192.168.16.51.1024506224022"
IssueInstant="2023-03-19T17:03:44.022Z">
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
    await cas.logb(body);
    if (userid === "casuser") {
        assert(body.includes("<saml1:AttributeValue>USER-ACCOUNT</saml1:AttributeValue>"));
    } else {
        assert(body.includes("<saml1:AttributeValue>ADMINISTRATOR</saml1:AttributeValue>"));
    }
})();
