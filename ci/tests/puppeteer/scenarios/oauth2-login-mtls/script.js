const assert = require("assert");
const cas = require("../../cas.js");
const https = require("https");
const fs = require("fs");
const path = require("path");

async function verifyFlowWithMutualTls() {
    const browser = await cas.newBrowser(cas.browserOptions());
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);

    const redirectUri = "https://localhost:9859/anything/cas";
    const baseUrl = "https://localhost:8443/cas/oauth2.0";

    const url = `${baseUrl}/authorize?response_type=code&redirect_uri=${redirectUri}&client_id=client&scope=profile`;
    await cas.goto(page, url);
    await cas.logPage(page);
    await cas.sleep(1000);
    await cas.loginWith(page);
    await cas.sleep(1000);

    const code = await cas.assertParameter(page, "code");
    await cas.log(`OAuth code ${code}`);
    await context.close();
    await cas.closeBrowser(browser);

    const httpsAgent = new https.Agent({
        cert: fs.readFileSync(path.join(__dirname, "/svid-2.pem")),
        key: fs.readFileSync(path.join(__dirname, "/key.pem")),
        ca: fs.readFileSync(path.join(__dirname, "/bundle.pem")),
        rejectUnauthorized: false
    });

    await cas.doPost(`${baseUrl}/token`,
        new URLSearchParams({
            grant_type: "authorization_code",
            scope: "profile",
            client_id: "client",
            redirect_uri: redirectUri,
            code: code
        }),
        {
            "Content-Type": "application/x-www-form-urlencoded"
        }, (res) => {
            const accessToken = res.data.access_token;
            assert(accessToken !== undefined);

            const params = new URLSearchParams();
            params.append("access_token", accessToken);

            cas.doPost(`${baseUrl}/profile`, params, {},
                (res) => {
                    const result = res.data;
                    assert(result.client_id === "client");
                    assert(result.service === redirectUri);
                    assert(result.attributes.oauthClientId === "client");
                    assert(result.attributes.organization === "apereo");
                    assert(result.attributes.email === "casuser@apereo.org");
                }, (error) => {
                    throw error;
                });
        }, (error) => {
            throw error;
        }, true, httpsAgent);
}

async function verifyFlowWithSpiffe() {
    const httpsAgent = new https.Agent({
        cert: fs.readFileSync(path.join(__dirname, "/svid.pem")),
        key: fs.readFileSync(path.join(__dirname, "/key.pem")),
        ca: fs.readFileSync(path.join(__dirname, "/bundle.pem")),
        rejectUnauthorized: false
    });

    await cas.doPost("https://localhost:8443/cas/oauth2.0/token",
        new URLSearchParams({
            grant_type: "client_credentials",
            scope: "profile"
        }),
        {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        (res) => {
            const accessToken = res.data.access_token;
            assert(accessToken !== undefined);

            const params = new URLSearchParams();
            params.append("access_token", accessToken);

            cas.doPost("https://localhost:8443/cas/oauth2.0/profile", params, {},
                (res) => {
                    const result = res.data;
                    assert(result.attributes["x509-issuerX500"] === "CN=example.org,O=Example SPIFFE CA,ST=CA,C=US");
                    assert(result.attributes["x509-issuer"] === "CN=example.org, O=Example SPIFFE CA, ST=CA, C=US");
                    assert(result.attributes["x509-subjectDN"] === "CN=service-a,O=Example,ST=CA,C=US");
                    assert(result.attributes["x509-sanURI"] === "spiffe://example.org/ns/payments/sa/service-sample");
                    assert(result.id === "CN=service-a,O=Example,ST=CA,C=US");
                    assert(result.client_id === "spiffe://example.org/ns/payments/sa/service-sample");
                }, (error) => {
                    throw error;
                });
        },
        (error) => {
            throw error;
        }, true, httpsAgent);
}

(async () => {
    await verifyFlowWithMutualTls();
    await cas.separator();
    await verifyFlowWithSpiffe();
})();
