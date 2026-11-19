
const path = require("path");
const assert = require("assert");
const cas = require("../../cas.js");

const ENTITY_PREFIX = "https://signing.example.org";
const RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
const RSA_SHA512 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512";
const DIGEST_SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
const DIGEST_SHA512 = "http://www.w3.org/2001/04/xmlenc#sha512";
const C14N_EXCLUSIVE_WITH_COMMENTS = "http://www.w3.org/2001/10/xml-exc-c14n#WithComments";
const CALLBACK_URL = "https://localhost:8443/cas/idp/profile/SAML2/Callback";
const HTTPBIN_URL = "https://localhost:9859/";

function children(node, localName) {
    if (node === undefined || node === null || typeof node !== "object") {
        return [];
    }
    return Object.keys(node)
        .filter((key) => key !== "$" && key !== "_" && (key === localName || key.endsWith(`:${localName}`)))
        .flatMap((key) => node[key]);
}

function child(node, localName) {
    return children(node, localName)[0];
}

function attributeOf(node, name) {
    return node?.["$"]?.[name];
}

function describeSignature(element) {
    const signature = child(element, "Signature");
    if (signature === undefined) {
        return undefined;
    }
    const signedInfo = child(signature, "SignedInfo");
    const keyInfo = child(signature, "KeyInfo");
    return {
        signatureMethod: attributeOf(child(signedInfo, "SignatureMethod"), "Algorithm"),
        canonicalizationMethod: attributeOf(child(signedInfo, "CanonicalizationMethod"), "Algorithm"),
        digestMethod: attributeOf(child(child(signedInfo, "Reference"), "DigestMethod"), "Algorithm"),
        reference: attributeOf(child(signedInfo, "Reference"), "URI"),
        x509Data: child(keyInfo, "X509Data") !== undefined,
        keyValue: child(keyInfo, "KeyValue") !== undefined || child(keyInfo, "DEREncodedKeyValue") !== undefined
    };
}

async function textOf(page, selector) {
    const element = await page.$(selector);
    return element === null ? undefined : cas.innerText(page, selector);
}

async function fetchSamlResponse(browser, entityId) {
    const context = await browser.createBrowserContext();
    const page = await cas.newPage(context);
    const callbackStatuses = [];
    const httpbinRequests = [];
    page.on("response", (response) => {
        if (response.url().startsWith(CALLBACK_URL)) {
            callbackStatuses.push(response.status());
        }
    });
    page.on("request", (request) => {
        if (request.url().startsWith(HTTPBIN_URL)) {
            httpbinRequests.push(request.url());
        }
    });
    try {
        const url = `https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO?providerId=${encodeURIComponent(entityId)}`;
        await cas.log(`Navigating to ${url}`);
        await cas.goto(page, url);
        await cas.sleep(2000);
        await cas.loginWith(page);
        await cas.sleep(3000);
        await cas.logPage(page);
        await cas.screenshot(page);
        const pageUrl = page.url();
        if (!pageUrl.startsWith(HTTPBIN_URL)) {
            return {
                pageUrl,
                callbackStatuses,
                httpbinRequests,
                errorHeader: await textOf(page, "#content h2"),
                errorContent: await textOf(page, "#content")
            };
        }
        const content = JSON.parse(await cas.innerText(page, "body"));
        assert(content.form.SAMLResponse !== undefined);
        const xml = await cas.base64Decode(content.form.SAMLResponse);
        await cas.log(xml);
        const response = child(await cas.parseXML(xml), "Response");
        assert(response !== undefined, "No SAML2 Response is found");
        const assertion = child(response, "Assertion");
        assert(assertion !== undefined, "No SAML2 Assertion is found");
        const statusCode = child(child(response, "Status"), "StatusCode");
        assert.equal(attributeOf(statusCode, "Value"), "urn:oasis:names:tc:SAML:2.0:status:Success");
        const result = {
            pageUrl,
            responseSignature: describeSignature(response),
            assertionSignature: describeSignature(assertion)
        };
        await cas.log(result);
        return result;
    } finally {
        await context.close();
    }
}

async function verifyServiceAlgorithmsAndBasicCredential(browser) {
    await cas.log("Per-service SHA-1 algorithms and a BASIC signing credential override the global settings");
    const result = await fetchSamlResponse(browser, `${ENTITY_PREFIX}/sp-sha1`);
    assert(result.responseSignature !== undefined, "The response must be signed by default");
    assert.equal(result.responseSignature.signatureMethod, RSA_SHA1);
    assert.equal(result.responseSignature.digestMethod, DIGEST_SHA1);
    assert.equal(result.responseSignature.x509Data, false, "A BASIC credential has no certificate to put in the signature");
    assert.equal(result.responseSignature.keyValue, true, "A BASIC credential carries the public key itself");
    assert(result.assertionSignature === undefined, "Assertions are not signed by default");
}

async function verifyAssertionSigningOnly(browser) {
    await cas.log("Signing the assertion only, with the global algorithms and a per-service canonicalization method");
    const result = await fetchSamlResponse(browser, `${ENTITY_PREFIX}/sp-assertion`);
    assert(result.responseSignature === undefined, "The response must not be signed");
    assert(result.assertionSignature !== undefined, "The assertion must be signed");
    assert.equal(result.assertionSignature.signatureMethod, RSA_SHA512);
    assert.equal(result.assertionSignature.digestMethod, DIGEST_SHA512);
    assert.equal(result.assertionSignature.canonicalizationMethod, C14N_EXCLUSIVE_WITH_COMMENTS);
    assert.equal(result.assertionSignature.x509Data, true, "The default X509 credential puts the certificate in the signature");
}

async function verifyGlobalAlgorithms(browser) {
    await cas.log("A service without overrides is signed with the algorithms of cas.authn.saml-idp.algs");
    const result = await fetchSamlResponse(browser, `${ENTITY_PREFIX}/sp-default`);
    assert(result.responseSignature !== undefined, "The response must be signed by default");
    assert.equal(result.responseSignature.signatureMethod, RSA_SHA512);
    assert.equal(result.responseSignature.digestMethod, DIGEST_SHA512);
    assert.equal(result.responseSignature.x509Data, true);
    assert(result.assertionSignature === undefined, "Assertions are not signed by default");
}

async function verifyBlockedAlgorithm(browser) {
    await cas.log("The only signature algorithm of the service is also blocked for it, so signing is refused and no response is sent");
    const result = await fetchSamlResponse(browser, `${ENTITY_PREFIX}/sp-blocked`);
    await cas.log(result);
    assert(result.pageUrl.startsWith(CALLBACK_URL), `The flow must end at the SAML2 callback, not at ${result.pageUrl}`);
    assert.equal(result.httpbinRequests.length, 0, "No SAML2 response may reach the service provider");
    assert.equal(result.callbackStatuses.at(-1), 400, "The callback must answer with the error view status");
    assert.equal(result.errorHeader, "Application Not Authorized to Use CAS");
    assert(result.errorContent.includes("The SAML authentication request cannot be understood, accepted or validated by CAS"),
        "The error view must carry the UNSATISFIED_SAML_REQUEST message of the SamlException");
}

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        await verifyServiceAlgorithmsAndBasicCredential(browser);
        await verifyAssertionSigningOnly(browser);
        await verifyGlobalAlgorithms(browser);
        await verifyBlockedAlgorithm(browser);
        await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    } finally {
        await cas.closeBrowser(browser);
    }
})();
