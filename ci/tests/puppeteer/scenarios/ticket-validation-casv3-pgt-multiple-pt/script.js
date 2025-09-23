const assert = require("assert");
const cas = require("../../cas.js");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");

const PROXIED_SERVICE = "https://localhost:9859/anything/sample";

const PT_PATTERN = /<cas:proxyTicket>(.*?)<\/cas:proxyTicket>/;

async function proxyValidateTicket(service, ticket, format = "JSON") {
    const body = await cas.doRequest(`https://localhost:8443/cas/p3/proxyValidate?service=${service}&ticket=${ticket}&format=${format}&pgtUrl=${PROXIED_SERVICE}`);
    await cas.log(body);
    return body;
}

async function getProxyTicket(service, ticket) {
    const body = await cas.doRequest(`https://localhost:8443/cas/proxy?service=${service}&pgt=${ticket}`);
    await cas.log(body);
    return body;
}

const createProxyTicket = async (proxyGrantingTicket) => {
    const proxyTicketResponse = await getProxyTicket(PROXIED_SERVICE, proxyGrantingTicket);
    assert(proxyTicketResponse !== undefined);
    const ptMatch = proxyTicketResponse.match(PT_PATTERN);
    assert(ptMatch !== null);
    const proxyTicket = ptMatch && ptMatch[1];
    assert(proxyTicket !== undefined);
    return proxyTicket;
};

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/cas";

    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(3000);

    const ticket = await cas.assertTicketParameter(page);
    const body = await proxyValidateTicket(service, ticket, "XML");
    assert(body.includes("<cas:proxyGrantingTicket>"));

    const pgtPattern = /<cas:proxyGrantingTicket>(.*?)<\/cas:proxyGrantingTicket>/;
    const pgtMatch = body.match(pgtPattern);
    assert(pgtMatch !== null);
    const pgt = pgtMatch && pgtMatch[1];

    const keyPath = path.join(__dirname, "private.key");
    const keyContent = fs.readFileSync(keyPath, "utf8");
    const decrypted = crypto.privateDecrypt(
        {
            key: keyContent,
            padding: crypto.constants.RSA_NO_PADDING,
            oaepHash: "sha1"
        },
        Buffer.from(pgt, "base64")
    ).toString("utf8");

    const proxyGrantingTicket = decrypted.substring(decrypted.indexOf("PGT-"));
    await cas.logg(proxyGrantingTicket);

    const sendConcurrentRequests = async () => {
        const promises = [];
        for (let i = 1; i <= 100; i++) {
            promises.push(createProxyTicket(proxyGrantingTicket));
        }
        await Promise.all(promises);
    };
    await sendConcurrentRequests();

    await cas.closeBrowser(browser);
})();
