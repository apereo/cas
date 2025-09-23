
const cas = require("../../cas.js");
const path = require("path");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://localhost:9859/anything/1";
    await cas.gotoLogin(page, service);
    await cas.loginWith(page);
    await cas.sleep(2000);
    const ticket = await cas.assertTicketParameter(page);

    const keyPath = path.join(__dirname, "private.key");
    const { payload } = await cas.decryptJwt(ticket, keyPath, "RSA-OAEP-256");
    assert(payload.iss === "https://localhost:8443/cas");
    assert(payload.aud === "https://localhost:9859/anything/1");
    assert(payload.credentialType === "UsernamePasswordCredential");
    assert(payload.sub === "casuser");
    assert(payload.jti.startsWith("ST-"));
    
    await cas.closeBrowser(browser);
})();
