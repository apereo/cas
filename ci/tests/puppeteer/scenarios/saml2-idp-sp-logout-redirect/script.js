const cas = require("../../cas.js");
const assert = require("assert");
const path = require("path");
const fs = require("fs");
const zlib = require("zlib");
const crypto = require("crypto");

const SP_ENTITY_ID = "https://samltest.id/saml/sp";
const SLO_POST_ENDPOINT = "https://localhost:8443/cas/idp/profile/SAML2/POST/SLO";
const SLO_REDIRECT_ENDPOINT = "https://localhost:8443/cas/idp/profile/SAML2/Redirect/SLO";
const SIG_ALG = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

async function buildLogoutRequest(destination) {
    return `<?xml version="1.0" encoding="UTF-8"?>
<saml2p:LogoutRequest Destination="${destination}"
    ID="_${await cas.uuid()}" IssueInstant="${new Date().toISOString()}" Version="2.0"
    xmlns:saml2p="urn:oasis:names:tc:SAML:2.0:protocol">
    <saml2:Issuer xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">${SP_ENTITY_ID}</saml2:Issuer>
    <saml2:NameID xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion"
        Format="urn:oasis:names:tc:SAML:2.0:nameid-format:transient">${await cas.uuid()}</saml2:NameID>
    <saml2p:SessionIndex>${await cas.uuid()}</saml2p:SessionIndex>
</saml2p:LogoutRequest>`;
}

/*
 * HTTP-Redirect binding: the signature covers the URL-encoded query string
 * "SAMLRequest=...&SigAlg=..." exactly as it is sent (SAML bindings, section 3.4.4.1).
 */
async function buildRedirectLogoutUrl(privateKey = undefined) {
    const xml = await buildLogoutRequest(SLO_REDIRECT_ENDPOINT);
    const samlRequest = zlib.deflateRawSync(Buffer.from(xml, "utf8")).toString("base64");
    let query = `SAMLRequest=${encodeURIComponent(samlRequest)}`;
    if (privateKey !== undefined) {
        query += `&SigAlg=${encodeURIComponent(SIG_ALG)}`;
        const signature = crypto.sign("sha256", Buffer.from(query, "utf8"), privateKey).toString("base64");
        query += `&Signature=${encodeURIComponent(signature)}`;
    }
    return `${SLO_REDIRECT_ENDPOINT}?${query}`;
}

async function assertSingleSignOnSession(page) {
    await cas.gotoLogin(page);
    await cas.sleep(1000);
    await cas.assertCookie(page);
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    let success = false;
    try {
        const privateKey = fs.readFileSync(path.join(__dirname, "sp-signing.key"), "utf8");
        const page = await cas.newPage(browser);
        await cas.gotoLogout(page);
        await cas.gotoLogin(page);
        await cas.loginWith(page);
        await assertSingleSignOnSession(page);

        await cas.log("force-signed-logout-requests is left at its default (true): unsigned logout requests must be refused");
        const postData = new URLSearchParams({
            SAMLRequest: Buffer.from(await buildLogoutRequest(SLO_POST_ENDPOINT), "utf8").toString("base64")
        }).toString();
        await cas.doRequest(SLO_POST_ENDPOINT, "POST", {
            "Content-Type": "application/x-www-form-urlencoded",
            "Content-Length": Buffer.byteLength(postData)
        }, 500, postData);

        const unsignedUrl = await buildRedirectLogoutUrl();
        let response = await cas.goto(page, unsignedUrl);
        await cas.sleep(1000);
        await cas.logPage(page);
        assert(response.status() === 500, `Unsigned logout request must be refused, but status is ${response.status()}`);
        await cas.log("The single sign-on session must survive the refused logout request");
        await assertSingleSignOnSession(page);

        await cas.log("A logout request signed with a key that is not in the SP metadata must be refused");
        const {privateKey: unknownKey} = crypto.generateKeyPairSync("rsa", {modulusLength: 2048});
        response = await cas.goto(page, await buildRedirectLogoutUrl(unknownKey));
        await cas.sleep(1000);
        await cas.logPage(page);
        assert(response.status() === 400, `Logout request with a foreign signature must be refused, but status is ${response.status()}`);
        await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");
        await assertSingleSignOnSession(page);

        await cas.log("A logout request signed with the key from the SP metadata is accepted");
        const signedUrl = await buildRedirectLogoutUrl(privateKey);
        assert(signedUrl.includes("&Signature="));
        await cas.goto(page, signedUrl);
        await cas.sleep(3000);
        await cas.logPage(page);
        assert(page.url().startsWith("https://localhost:9859/get"));

        await cas.gotoLogin(page);
        await cas.sleep(1000);
        await cas.assertVisibility(page, "#username");
        success = true;
    } finally {
        if (success) {
            await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
        }
        await cas.closeBrowser(browser);
    }
})();
