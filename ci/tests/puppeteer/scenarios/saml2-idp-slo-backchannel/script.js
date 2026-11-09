const assert = require("assert");
const path = require("path");
const express = require("express");
const {processors} = require("xml2js");
const cas = require("../../cas.js");

const IDP_ENTITY_ID = "https://cas.apereo.org/saml/idp";
const UNSOLICITED_SSO_URL = "https://localhost:8443/cas/idp/profile/SAML2/Unsolicited/SSO";
const ACS_URL = "https://localhost:9859/post";
const SLO_PORT = 5611;
const STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";
const EMAIL_FORMAT = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";

const SERVICE_PROVIDERS = {
    signed: "https://localhost:9859/slo-soap-signed",
    unsigned: "https://localhost:9859/slo-soap-unsigned",
    none: "https://localhost:9859/slo-soap-none"
};

function textOf(element) {
    return typeof element === "string" ? element : element._;
}

/*
 * Each service provider publishes a SOAP SingleLogoutService on this server; CAS posts its back-channel
 * LogoutRequest there when the single sign-on session ends.
 */
async function startSingleLogoutServer(received) {
    const app = express();
    app.use(express.text({type: "*/*", limit: "1mb"}));
    app.post("/slo/:sp", (req, res) => {
        const message = {contentType: req.get("Content-Type"), body: req.body};
        cas.log(`Received back-channel logout request for ${req.params.sp}: ${JSON.stringify(message)}`);
        (received[req.params.sp] ??= []).push(message);
        res.status(200).type("text/xml").send("<soap11:Envelope xmlns:soap11=\"http://schemas.xmlsoap.org/soap/envelope/\">"
            + "<soap11:Body></soap11:Body></soap11:Envelope>");
    });
    return new Promise((resolve) => {
        const server = app.listen(SLO_PORT, () => resolve(server));
    });
}

async function loginWithUnsolicitedSso(page, entityId) {
    const url = `${UNSOLICITED_SSO_URL}?providerId=${encodeURIComponent(entityId)}`;
    await cas.goto(page, url);
    await cas.sleep(2000);
    if (await cas.isVisible(page, "#username")) {
        await cas.loginWith(page);
        await cas.sleep(3000);
    }
    await cas.logPage(page);
    assert(page.url().startsWith(ACS_URL), `Response for ${entityId} must be posted to ${ACS_URL}, but page is at ${page.url()}`);
    const content = JSON.parse(await cas.innerText(page, "body"));
    assert(content.form.SAMLResponse !== undefined);
    const xml = await cas.base64Decode(content.form.SAMLResponse);
    await cas.log(xml);
    const response = (await cas.parseXML(xml, {tagNameProcessors: [processors.stripPrefix]})).Response;
    assert(response.Status[0].StatusCode[0].$.Value === STATUS_SUCCESS);
    const assertion = response.Assertion[0];
    const nameId = assertion.Subject[0].NameID[0];
    const result = {
        nameId: textOf(nameId),
        format: nameId.$.Format,
        sessionIndex: assertion.AuthnStatement[0].$.SessionIndex
    };
    await cas.log(`${entityId} received ${JSON.stringify(result)}`);
    assert(result.nameId === "casuser@example.org");
    assert(result.format === EMAIL_FORMAT);
    assert(result.sessionIndex !== undefined && result.sessionIndex.length > 0);
    return result;
}

/*
 * The SAML SOAP binding carries the SOAP envelope itself as the HTTP body (SAML Bindings, section 3.2.3).
 */
async function parseLogoutRequest(message) {
    assert(message.contentType !== undefined && message.contentType.startsWith("text/xml"),
        `SOAP logout requests must be sent as text/xml, but got ${message.contentType}`);
    const xml = message.body;
    assert(xml.trimStart().startsWith("<"),
        `The SOAP envelope must be the HTTP body as XML, but it arrived form-encoded: ${xml.substring(0, 80)}`);
    const parsed = await cas.parseXML(xml, {tagNameProcessors: [processors.stripPrefix]});
    console.dir(parsed, {depth: null, colors: true});
    assert(parsed !== undefined && parsed.Envelope !== undefined, `Logout request must be a SOAP envelope: ${xml}`);
    return parsed.Envelope.Body[0].LogoutRequest[0];
}

async function assertLogoutRequest(message, sp, session, signed) {
    const logoutRequest = await parseLogoutRequest(message);
    assert(logoutRequest.$.Version === "2.0");
    assert(logoutRequest.$.Destination === `http://localhost:${SLO_PORT}/slo/${sp}`,
        `Destination must be the SOAP SingleLogoutService, but is ${logoutRequest.$.Destination}`);
    assert(textOf(logoutRequest.Issuer[0]) === IDP_ENTITY_ID);
    const nameId = logoutRequest.NameID[0];
    assert(textOf(nameId) === session.nameId, `NameID must be ${session.nameId}, but is ${textOf(nameId)}`);
    assert(nameId.$.Format === session.format, `NameID format must be ${session.format}, but is ${nameId.$.Format}`);
    assert(logoutRequest.SessionIndex.length === 1);
    assert(textOf(logoutRequest.SessionIndex[0]) === session.sessionIndex,
        `SessionIndex must be ${session.sessionIndex}, but is ${textOf(logoutRequest.SessionIndex[0])}`);
    if (signed) {
        assert(logoutRequest.Signature !== undefined, `Logout request for ${sp} must be signed`);
        const signature = logoutRequest.Signature[0];
        assert(signature.SignedInfo[0].Reference[0].$.URI === `#${logoutRequest.$.ID}`);
        assert(textOf(signature.SignatureValue[0]).trim().length > 0);
    } else {
        assert(logoutRequest.Signature === undefined, `Logout request for ${sp} must not be signed`);
    }
}

(async () => {
    const received = {};
    const server = await startSingleLogoutServer(received);
    const browser = await cas.newBrowser(cas.browserOptions());
    let success = false;
    try {
        const page = await cas.newPage(browser);
        await cas.log("Ending any single sign-on session left behind by an earlier attempt");
        await cas.gotoLogout(page);
        await cas.sleep(2000);
        Object.keys(received).forEach((key) => delete received[key]);

        const sessions = {};
        for (const [sp, entityId] of Object.entries(SERVICE_PROVIDERS)) {
            sessions[sp] = await loginWithUnsolicitedSso(page, entityId);
        }
        assert(new Set(Object.values(sessions).map((session) => session.sessionIndex)).size === 3,
            "Each service provider must get its own session index");
        assert(Object.keys(received).length === 0, "Nothing may be sent before logout");

        await cas.logg("Logging out of CAS sends SOAP logout requests to the back-channel service providers");
        await cas.gotoLogout(page);
        for (let attempt = 0; attempt < 15 && (received.signed === undefined || received.unsigned === undefined); attempt++) {
            await cas.sleep(1000);
        }
        await cas.sleep(2000);
        await cas.log(`Received logout requests for ${Object.keys(received)}`);

        assert(received.none === undefined, "A service provider with logout type NONE must not receive a logout request");
        assert(received.signed !== undefined && received.signed.length === 1,
            `Expected one logout request for the signed service provider, but got ${received.signed?.length}`);
        assert(received.unsigned !== undefined && received.unsigned.length === 1,
            `Expected one logout request for the unsigned service provider, but got ${received.unsigned?.length}`);

        await cas.log("Logout requests are signed by default (force-signed-logout-requests), unless the service turns it off");
        await assertLogoutRequest(received.signed[0], "signed", sessions.signed, true);
        await assertLogoutRequest(received.unsigned[0], "unsigned", sessions.unsigned, false);

        success = true;
    } finally {
        if (success) {
            await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
        }
        server.close();
        await cas.closeBrowser(browser);
    }
})();
