
const cas = require("../../cas.js");
const assert = require("assert");
const express = require("express");
const fs = require("fs");
const path = require("path");

const WORK_DIR = path.join(__dirname, "..", "..", "overlay", path.basename(__dirname));
const CRL_SERVER_PORT = 5615;
const CRL_FILES = ["ca.crl", "expired-ca.crl", "unlisted-ca.crl"];

/*
 * Whether each client certificate (made by init.sh) logs in, per variation:
 * crlvalid and crlrevoked come from CasTestCA, whose current CRL lists crlrevoked; crlexpired comes from
 * CasTestExpiredCrlCA, whose CRL expired on 2025-01-02; no CRL exists for CasTestUnlistedCA, the issuer of crlunlisted.
 */
const EXPECTATIONS = {
    "resource": {crlvalid: true, crlrevoked: false, crlexpired: false, crlunlisted: false},
    "resource-lenient": {crlvalid: true, crlrevoked: false, crlexpired: true, crlunlisted: true},
    "crl": {crlvalid: true, crlrevoked: false, crlexpired: false, crlunlisted: false}
};

function certificateHeader(name) {
    const cert = fs.readFileSync(path.join(WORK_DIR, `${name}.pem`)).toString();
    return cert.trim().replace(/\r/g, "").replace(/\n/g, " ");
}

async function verifyLogin(page, name, loginExpected) {
    await cas.log(`Presenting certificate ${name}; login is ${loginExpected ? "expected" : "expected to fail"}`);
    await cas.gotoLogin(page);
    await cas.sleep(3000);
    if (loginExpected) {
        await cas.assertInnerText(page, "#content div h2", "Log In Successful");
        await cas.assertInnerTextContains(page, "#content div p", `CN=${name}`);
        await cas.assertCookie(page);
        await cas.gotoLogout(page);
    } else {
        await cas.assertInnerText(page, "#loginErrorsPanel p", "Authentication attempt has failed.");
        await cas.assertCookie(page, false);
    }
}

async function verifyRevocation(crlRequests) {
    const variation = process.env.SCENARIO_VARIATION;
    const expectations = EXPECTATIONS[variation];
    assert(expectations !== undefined, `Unknown variation ${variation}`);
    await cas.log(`Running with revocation variation ${variation}`);

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await page.setRequestInterception(true);

    let certificate = undefined;
    page.on("request", (request) => {
        const headers = {...request.headers()};
        if (certificate !== undefined) {
            headers["ssl-client-cert-from-proxy"] = certificate;
        }
        request.continue({headers: headers});
    });

    for (const [name, loginExpected] of Object.entries(expectations)) {
        certificate = certificateHeader(name);
        await verifyLogin(page, name, loginExpected);
    }
    await cas.closeBrowser(browser);

    await cas.log(`CRL requests received: ${crlRequests}`);
    if (variation === "crl") {
        /*
            CAS caches fetched CRLs, so a retried attempt may not fetch ca.crl or expired-ca.crl again;
            the missing CRL is never cached and is requested on every attempt.
         */
        assert(crlRequests.includes("unlisted-ca.crl"), "CAS did not fetch the CRL distribution point of crlunlisted");
    } else {
        assert.equal(crlRequests.length, 0, "CRL distribution points must not be used with revocation-checker=RESOURCE");
    }
}

(async () => {
    const crlRequests = [];
    const app = express();
    CRL_FILES.forEach((name) => {
        app.get(`/${name}`, (req, res) => {
            crlRequests.push(name);
            const file = path.join(WORK_DIR, name);
            if (fs.existsSync(file)) {
                res.sendFile(file);
            } else {
                res.status(404).send("CRL not found");
            }
        });
    });
    const server = app.listen(CRL_SERVER_PORT, () => cas.log(`CRL server is listening on port ${CRL_SERVER_PORT}`));
    try {
        await verifyRevocation(crlRequests);
    } finally {
        server.closeAllConnections();
        server.close();
    }
})();
